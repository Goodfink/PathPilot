package org.example.pathpilot.helpers;

import org.example.pathpilot.model.file.FileHandleType;

import java.nio.file.Path;
import java.util.Set;

public class FileHelpers {

    private static final Set<String> PLAIN_TEXT_EXTENSIONS = Set.of(
            "txt", "md", "java", "py", "js", "ts",
            "json", "xml", "yaml", "yml", "csv", "sql", "log"
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "webp"
    );

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

    public boolean fileExists(Path path) {
        return path.toFile().exists();
    }
}
