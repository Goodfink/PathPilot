package org.example.pathpilot.model.llm;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LLMResponse {
    private int folderId;
    private double confidence;
}
