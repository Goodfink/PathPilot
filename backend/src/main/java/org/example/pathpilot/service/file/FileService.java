package org.example.pathpilot.service.file;

import java.io.IOException;
import java.nio.file.WatchEvent;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.helpers.EventHelpers;
import org.example.pathpilot.model.pendingMove.OperationType;
import org.example.pathpilot.model.pendingMove.PendingMove;
import org.example.pathpilot.model.event.PendingMoveCreatedEvent;
import org.example.pathpilot.model.file.FileInfo;
import org.example.pathpilot.model.llm.ClassificationResult;
import org.example.pathpilot.repository.PendingMovesRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.example.pathpilot.service.duplicate.DuplicateService;
import org.example.pathpilot.service.llm.LLMService;
import org.example.pathpilot.helpers.FileHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileHelpers fileHelpers = new FileHelpers();
    private final EventHelpers eventHelpers = new EventHelpers();
    private final PendingMovesRepository pendingMovesRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FileContentService fileContentService;
    private final LLMService llmService;
    private final DuplicateService duplicateService;

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    public void receiveFile(WatchEvent<?> event) throws IOException {
        FileInfo fileInfo = FileInfo.builder()
                .filePath(eventHelpers.getFilePath(event))
                .fileExtension(eventHelpers.getFileExtension(event))
                .fileName(eventHelpers.getFileName(event))
                .build();
        fileInfo.setFileHandleType(fileHelpers.getFileHandleType(fileInfo.getFileExtension()));

        log.info("Received File: filePath={}, fileExtension={}, fileName={}, fileHandleType={}",
                fileInfo.getFilePath(),
                fileInfo.getFileExtension(),
                fileInfo.getFileName(),
                fileInfo.getFileHandleType());

        try {
            FileInfo fileInfoWithContent = fileContentService.handleExtensionCase(fileInfo);
            ClassificationResult result = llmService.callFileClassifier(fileInfoWithContent);
            OperationType operationType = duplicateService.checkAndHandleDuplicate(result.getPath(), fileInfo.getFilePath());

            PendingMove pendingMove = PendingMove.builder()
                    .fromPath(fileInfo.getFilePath())
                    .toPath(operationType == OperationType.MOVE ? result.getPath() : null)
                    .fileName(fileInfo.getFileName())
                    .confidence(result.getConfidence())
                    .operationType(operationType)
                    .build();

            int id = pendingMovesRepository.insertPendingMove(pendingMove);

            log.info("Pending Move added to DB: pendingMove={}", pendingMove);
            eventPublisher.publishEvent(
                    PendingMoveCreatedEvent.builder()
                            .id(id)
                            .pendingMove(pendingMove)
                            .build()
            );
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
