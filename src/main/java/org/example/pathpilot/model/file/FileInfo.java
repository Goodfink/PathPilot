package org.example.pathpilot.model.file;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class FileInfo {
    private String fileName;
    private String fileExtension;
    private Path filePath;
    private FileHandleType fileHandleType;
    private String fileContent;
}
