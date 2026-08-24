package org.example.pathpilot.helpers;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static org.example.pathpilot.watcher.Watcher.PATH;

public class EventHelpers {

    public Path getFilePath(WatchEvent<?> event) {
        String fileName = event.context().toString();
        String fullFilePath = PATH + fileName;
        return Path.of(fullFilePath);
    }

    public String getFileExtension(WatchEvent<?> event) {
        String fileName = event.context().toString();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public String getFileName(WatchEvent<?> event) {
        String fileName = event.context().toString();
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }
}
