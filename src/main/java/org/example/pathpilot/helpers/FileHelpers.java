package org.example.pathpilot.helpers;

import org.example.pathpilot.model.file.FileHandleType;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Set;

import static org.example.pathpilot.watcher.Watcher.PATH;

public class FileHelpers {

    private static final Set<String> PLAIN_TEXT_EXTENSIONS = Set.of(
            "txt", "md", "java", "py", "js", "ts",
            "json", "xml", "yaml", "yml", "csv", "sql", "log"
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "webp", "heic"
    );

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

    public FileHandleType getFileHandleType(String fileExtension) {
        if (PLAIN_TEXT_EXTENSIONS.contains(fileExtension)) {
            return FileHandleType.PLAIN_TEXT;
        } else if (IMAGE_EXTENSIONS.contains(fileExtension)) {
            return FileHandleType.IMAGE;
        } else if (fileExtension.equals("docx")) {
            return FileHandleType.DOCX;
        } else if (fileExtension.equals("pdf")) {
            return FileHandleType.PDF;
        } else {
            return FileHandleType.DEFAULT;
        }
    }
}
