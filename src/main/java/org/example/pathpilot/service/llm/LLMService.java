package org.example.pathpilot.service.llm;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.folder.FolderInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.example.pathpilot.llm.LLMClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LLMService {

    private final LLMClient llmClient;
    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    public ClassificationResult callClassifier(FileInfo file, List<FolderInfo> folders) {
        ClassificationResult response = llmClient.classifyDestinationFolder(file, folders);
        return response;
    }
}
