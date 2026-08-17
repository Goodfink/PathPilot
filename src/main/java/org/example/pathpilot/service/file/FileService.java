package org.example.pathpilot.service.file;

import java.io.IOException;
import java.nio.file.WatchEvent;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.helpers.FileHelpers;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.file.FileHandleType;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.example.pathpilot.repository.PendingMovesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileHelpers fileHelpers = new FileHelpers();
    private final PlainTextFileService plainTextFileService;
    private final PendingMovesRepository pendingMovesRepository;

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    public void receiveFile(WatchEvent<?> event) {
        FileInfo fileInfo = FileInfo.builder()
                .filePath(fileHelpers.getFilePath(event))
                .fileExtension(fileHelpers.getFileExtension(event))
                .fileName(fileHelpers.getFileName(event))
                .build();
        fileInfo.setFileHandleType(fileHelpers.getFileHandleType(fileInfo.getFileExtension()));

        log.info("Received File: filePath={}, fileExtension={}, fileName={}, fileHandleType={}",
                fileInfo.getFilePath(),
                fileInfo.getFileExtension(),
                fileInfo.getFileName(),
                fileInfo.getFileHandleType());

        try {
            ClassificationResult result = handleExtensionCase(fileInfo.getFileHandleType(), fileInfo);
            pendingMovesRepository.insertPendingMove(result, fileInfo.getFilePath());
        } catch (IOException e) {
            // TODO error handling
        }

    }

    private ClassificationResult handleExtensionCase(FileHandleType fileHandleType, FileInfo fileInfo) throws IOException {
        ClassificationResult result =  ClassificationResult.builder().build();

        switch (fileHandleType) {
            case FileHandleType.DOCX:
                log.info("Handling DOCX file: {}", fileInfo.getFileName());
                // read word file
                break;
            case FileHandleType.PDF:
                log.info("Handling PDF file: {}", fileInfo.getFileName());
                // read pdf
                break;
            case FileHandleType.IMAGE:
                log.info("Handling image file: {}", fileInfo.getFileName());
                //handle Image
                break;
            case FileHandleType.PLAIN_TEXT:
                log.info("Handling plain text file: {}", fileInfo.getFileName());
                result = plainTextFileService.handlePlainTextFile(fileInfo);
                break;
        }

        return result;
    }
}
