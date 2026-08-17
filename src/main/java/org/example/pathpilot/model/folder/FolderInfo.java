package org.example.pathpilot.model.folder;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class FolderInfo {
    private String name;
    private Path path;
    private Long id;
    private Path parentPath;
    private int depth;
}
