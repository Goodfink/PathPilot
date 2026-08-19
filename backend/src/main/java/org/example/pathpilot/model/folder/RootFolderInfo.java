package org.example.pathpilot.model.folder;

import lombok.Builder;
import lombok.Data;
import java.nio.file.Path;

@Data
@Builder
public class RootFolderInfo {
    private Path path;
    private Long id;
}
