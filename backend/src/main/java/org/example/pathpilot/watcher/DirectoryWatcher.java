package org.example.pathpilot.watcher;

import org.example.pathpilot.model.folder.FolderInfo;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.example.pathpilot.repository.FoldersRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

@Component
public class DirectoryWatcher {

    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final FoldersRepository foldersRepository;
    public DirectoryWatcher(FoldersRepository foldersRepository) {
        this.foldersRepository = foldersRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void directoryWatcherStart() {
        executorService.submit(this::directoryWatcher);
    }

    private void directoryWatcher() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Map<WatchKey, Path> watchedDirectories = new HashMap<>();
            Map<Path, FolderInfo> folderMap = new HashMap<>();

            List<FolderInfo> folders = foldersRepository.getFolders();
            log.info("Directory watcher starting: numFolder={}", folders.size());

            for (FolderInfo folder : folders) {
                Path folderPath = folder.getPath();

                if (!Files.isDirectory(folderPath)) {
                    continue;
                }

                WatchKey key = folderPath.register(watchService,ENTRY_CREATE, ENTRY_DELETE);
                watchedDirectories.put(key, folderPath);
                folderMap.put(folderPath, folder);
            }

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                Path parentPath = watchedDirectories.get(key);

                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    Path relativePath = (Path) watchEvent.context();
                    Path fullPath = parentPath.resolve(relativePath);
                    if (watchEvent.kind() == ENTRY_CREATE && Files.isDirectory(fullPath)) {
                        FolderInfo parentFolder = folderMap.get(parentPath);
                        int depth = parentFolder.getDepth() + 1;
                        log.info("Folder Add: folderPath={}", fullPath);

                        FolderInfo folderInfo = buildFolderInfoObject(fullPath, parentPath, depth);

                        foldersRepository.insertFolder(folderInfo);
                        folderMap.put(fullPath, folderInfo);
                        WatchKey newKey = fullPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
                        watchedDirectories.put(newKey, fullPath);
                    } else if (watchEvent.kind() == ENTRY_DELETE) {
                        int deletedRows = foldersRepository.deleteFolderByPath(fullPath);
                        log.info("Folder Deleted: folderPath={}", fullPath);

                        if (deletedRows > 0) {
                            folderMap.remove(fullPath);
                        }
                    }
                }

                boolean valid = key.reset();

                if (!valid) {
                    Path removedPath = watchedDirectories.remove(key);

                    if (removedPath != null) {
                        folderMap.remove(removedPath);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private FolderInfo buildFolderInfoObject(Path fullPath, Path parentPath, int depth) {
        return FolderInfo.builder()
                .name(fullPath.getFileName().toString())
                .path(fullPath)
                .parentPath(parentPath)
                .depth(depth)
                .build();
    }

}
