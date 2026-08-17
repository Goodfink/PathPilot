package org.example.pathpilot.service.file;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.example.pathpilot.service.llm.LLMService;
import org.example.pathpilot.repository.FoldersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class PlainTextFileService {

    private final LLMService llmService;
    private final FoldersRepository foldersRepository;

    private static final Logger log = LoggerFactory.getLogger(PlainTextFileService.class);

    public ClassificationResult handlePlainTextFile(FileInfo fileInfo) throws IOException {
        try {
            String fileContent = Files.readString(fileInfo.getFilePath());
            fileInfo.setFileContent(fileContent);
            log.debug("Calling LLM: filePath={}", fileInfo.getFilePath());
            ClassificationResult classificationResult = llmService.callClassifier(fileInfo, foldersRepository.getFolders());
            return classificationResult;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
