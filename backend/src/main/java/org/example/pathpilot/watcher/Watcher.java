package org.example.pathpilot.watcher;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.service.file.FileService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.example.pathpilot.service.folder.FolderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import org.example.pathpilot.helpers.EventHelpers;

@Component
@RequiredArgsConstructor
public class Watcher {

    private final ExecutorService watcherExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService fileExecutor = Executors.newFixedThreadPool(2);
    private static final Logger log = LoggerFactory.getLogger(Watcher.class);
    public static final String PATH = "/Users/jakefinkelstein/Downloads/";
    private final FileService fileService;
    private final FolderService folderService;
    private final EventHelpers eventHelpers = new EventHelpers();

    @EventListener(ApplicationReadyEvent.class)
    public void fileWatcherStart() {
        watcherExecutor.submit(this::fileWatcher);
    }

    public void fileWatcher() {

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(PATH);
            path.register(watchService, ENTRY_CREATE);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    log.info("File downloaded: relativeFilePath={}", event.context());
                    fileExecutor.submit(() -> {
                        try {
                            if (Files.isDirectory(eventHelpers.getFilePath(event))) {
                                log.info("Received folder input");
                                folderService.receiveFolder(event);
                            } else {
                                log.info("Received file input");
                                fileService.receiveFile(event);
                            }
                        } catch (IOException e) {
                            log.error("Error processing file: file = {}", event.context(), e);
                        }
                    });
                }

                key.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
