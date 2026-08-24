package org.example.pathpilot.service.file;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.example.pathpilot.service.llm.LLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class PlainTextFileService {

    private final LLMService llmService;

    private static final Logger log = LoggerFactory.getLogger(PlainTextFileService.class);

    public FileInfo getPlainTextFileContent(FileInfo fileInfo) throws IOException {
        String fileContent;
        try {
            fileContent = Files.readString(fileInfo.getFilePath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        FileInfo fileInfoWithContent = fileInfo;
        fileInfoWithContent.setFileContent(fileContent);

        return fileInfoWithContent;
    }
}
