package org.example.pathpilot.model.llm;

import lombok.Data;
import lombok.Builder;
import java.nio.file.Path;

@Data
@Builder
public class ClassificationResult {
    private Path path;
    private double confidence;
}
