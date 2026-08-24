package org.example.pathpilot.service.folder;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.helpers.EventHelpers;
import org.example.pathpilot.model.event.PendingMoveCreatedEvent;
import org.example.pathpilot.repository.PendingMovesRepository;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.folder.FolderInfo;
import org.example.pathpilot.model.folder.RootFolderInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.example.pathpilot.model.pendingMove.OperationType;
import org.example.pathpilot.model.pendingMove.PendingMove;
import org.example.pathpilot.repository.RootFoldersRepository;
import org.example.pathpilot.repository.FoldersRepository;
import org.example.pathpilot.helpers.FileHelpers;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.example.pathpilot.service.file.FileContentService;
import org.example.pathpilot.service.llm.LLMService;
import org.example.pathpilot.helpers.PathHelpers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class FolderService {

    private static final int FOLDER_TOP_K = 3;

    // hard coded for now will end up being user input
    private static final List<String> ROOTS = new ArrayList<>(List.of("/Users/jakefinkelstein/Documents"));
    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(".git", ".idea", ".gradle", "node_modules", "venv", ".venv", "myenv", "site-packages", "__pycache__", "build", "target", "dist", "out", "bin", "Debug", "Release", "class-use", "index-files", "script-dir");

    private static final Logger log = LoggerFactory.getLogger(FolderService.class);
    private final Set<Path> activeRoots = ConcurrentHashMap.newKeySet();

    private final RootFoldersRepository rootFoldersRepository;
    private final FoldersRepository foldersRepository;
    private final LLMService llmService;
    private final PendingMovesRepository pendingMovesRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FileContentService fileContentService;
    private final EventHelpers eventHelpers = new EventHelpers();
    private final PathHelpers pathHelpers = new PathHelpers();
    private final FileHelpers fileHelpers = new FileHelpers();

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

    public void receiveFolder(WatchEvent<?> event) throws IOException {
        Path folderPath = eventHelpers.getFilePath(event);
        log.debug("FolderPath: folderPath={}", folderPath);

        String folderName = pathHelpers.getFolderName(folderPath);
        log.debug("Walking");

        List<Path> files;
        log.debug("Walking");
        try (Stream<Path> paths = Files.walk(folderPath)){
            files = paths.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Fetched folder files: numPaths={}", files.size());
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (int i = 0; i < FOLDER_TOP_K && i < files.size(); i++) {
            Path path = files.get(i);
            FileInfo fileInfo = FileInfo.builder()
                    .filePath(path)
                    .fileExtension(pathHelpers.getFileExtension(path))
                    .fileName(pathHelpers.getFileName(path))
                    .fileHandleType(fileHelpers.getFileHandleType(pathHelpers.getFileExtension(path)))
                    .build();
            FileInfo fileInfoWithContent = fileContentService.handleExtensionCase(fileInfo);
            log.debug("File name: fileName={}", fileInfoWithContent.getFileName());
            fileInfoList.add(fileInfoWithContent);
        }

        log.info("Made file list top_k: fileSize={}", fileInfoList.size());
        ClassificationResult result = llmService.callFolderClassifier(fileInfoList, folderName);

        PendingMove pendingMove = PendingMove.builder()
                .fromPath(folderPath)
                .toPath(result.getPath())
                .fileName(folderName)
                .confidence(result.getConfidence())
                .build();

        boolean folderExistsAtDestination = fileHelpers.fileExists(pendingMove.getToPath().resolve(pendingMove.getFromPath().getFileName()));

        if (folderExistsAtDestination) {
            log.info("Folder already exists at destination: fileName={}", folderName);
            pendingMove.setToPath(null);
            pendingMove.setOperationType(OperationType.TRASH);
        } else if (result.getPath() == null) {
            log.info("No suitable destination folder found: fileName={}", folderName);
            pendingMove.setToPath(null);
            pendingMove.setOperationType(OperationType.TRASH);
        }  else {
            pendingMove.setOperationType(OperationType.MOVE);
        }

        int id = pendingMovesRepository.insertPendingMove(pendingMove);
        log.info("Pending Move added to DB: pendingMove={}", pendingMove);

        eventPublisher.publishEvent(
                PendingMoveCreatedEvent.builder()
                        .id(id)
                        .pendingMove(pendingMove)
                        .build());
    }
}
