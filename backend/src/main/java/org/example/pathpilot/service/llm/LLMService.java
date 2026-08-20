package org.example.pathpilot.service.llm;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.pathpilot.llm.LLMClient;
import org.example.pathpilot.repository.FoldersRepository;


@Service
@RequiredArgsConstructor
public class LLMService {

    private final LLMClient llmClient;
    private static final Logger log = LoggerFactory.getLogger(LLMService.class);
    private final FoldersRepository foldersRepository;

    public ClassificationResult callClassifier(FileInfo file) {
        ClassificationResult response = llmClient.classifyDestinationFolder(file, foldersRepository.getFolders());
        return response;
    }
}
