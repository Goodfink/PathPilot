package org.example.pathpilot.model.pendingMove;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class PendingMove {
    private int id;
    private Path fromPath;
    private Path toPath;
    private String fileName;
    private double confidence;
    private OperationType operationType;
}
