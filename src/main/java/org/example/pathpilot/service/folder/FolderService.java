package org.example.pathpilot.service.folder;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.folder.FolderInfo;
import org.example.pathpilot.model.folder.RootFolderInfo;
import org.example.pathpilot.repository.RootFoldersRepository;
import org.example.pathpilot.repository.FoldersRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class FolderService {

    // hard coded for now will end up being user input
    private static final List<String> ROOTS = new ArrayList<>(List.of("/Users/jakefinkelstein/Documents"));
    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(".git", ".idea", ".gradle", "node_modules", "venv", ".venv", "myenv", "site-packages", "__pycache__", "build", "target", "dist", "out", "bin", "Debug", "Release", "class-use", "index-files", "script-dir");

    private static final Logger log = LoggerFactory.getLogger(FolderService.class);
    private final Set<Path> activeRoots = ConcurrentHashMap.newKeySet();

    private final RootFoldersRepository rootFoldersRepository;
    private final FoldersRepository foldersRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeFolderIndex() {
        populateRootFolders();
        loadRootFolders();
        populateFolders();

        log.info("Folders index populated");
    }

    public void loadRootFolders() {
        List<RootFolderInfo> currentRootFolders = rootFoldersRepository.getRootFolders();

        for (RootFolderInfo currentRootFolder : currentRootFolders) {
            activeRoots.add(currentRootFolder.getPath());
        }
    }

    public void populateRootFolders() {

        List<RootFolderInfo> currentRootFolders = rootFoldersRepository.getRootFolders();

        if (!currentRootFolders.isEmpty()) {
            for (RootFolderInfo currentRootFolder : currentRootFolders) {
                if (ROOTS.contains(currentRootFolder.getPath().toString())) {
                    ROOTS.remove(currentRootFolder.getPath().toString());
                }
            }
        }

        rootFoldersRepository.populateRootFolders(ROOTS);
    }

    public void populateFolders() {
            List<FolderInfo> currentFolders = foldersRepository.getFolders();
            List<FolderInfo> folders = getFolderPaths();
            List<FolderInfo> cleanedFolders = dedupeFolders(currentFolders, folders);
            foldersRepository.populateFolders(cleanedFolders);
            log.debug("Finished populating folders");
    }

    private List<FolderInfo> getFolderPaths() {
        List<FolderInfo> folders = new ArrayList<>();

        for (Path path : activeRoots) {
            try {
                Files.walkFileTree(path, createFolderVisitor(path, folders));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return folders;
    }

    private FileVisitor<Path> createFolderVisitor(Path rootPath, List<FolderInfo> folders) {
        return new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(
                    Path dir,
                    BasicFileAttributes attrs
            ) throws IOException {

                String directoryName = dir.getFileName().toString();

                if (Files.isHidden(dir)
                        || EXCLUDED_DIRECTORIES.contains(directoryName)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                if (!dir.equals(rootPath)) {
                    folders.add(FolderInfo.builder()
                            .name(directoryName)
                            .path(dir)
                            .parentPath(dir.getParent())
                            .depth(rootPath.relativize(dir).getNameCount())
                            .build());
                }

                return FileVisitResult.CONTINUE;
            }
        };
    }

    private List<FolderInfo> dedupeFolders(List<FolderInfo> currentFolders, List<FolderInfo> folders) {
        Set<Path> existingPaths = currentFolders.stream()
                .map(FolderInfo::getPath)
                .collect(Collectors.toSet());

        return folders.stream()
                .filter(folder -> !existingPaths.contains(folder.getPath()))
                .toList();
    }
}
