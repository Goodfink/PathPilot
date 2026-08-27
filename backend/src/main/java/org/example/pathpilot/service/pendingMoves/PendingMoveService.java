package org.example.pathpilot.service.pendingMoves;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.pendingMove.OperationType;
import org.example.pathpilot.model.pendingMove.PendingMove;
import org.example.pathpilot.model.event.PendingMoveDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.example.pathpilot.repository.PendingMovesRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingMoveService {

    private static final Logger log = LoggerFactory.getLogger(PendingMoveService.class);
    private final PendingMovesRepository pendingMovesRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<PendingMove> getPendingMoves() {
        List<PendingMove> pendingMoves = pendingMovesRepository.getPendingMoves();
        log.info("Received pending moves: count={}", pendingMoves.size());
        return pendingMoves;
    }

    public PendingMove getPendingMoveById(int id) {
        PendingMove pendingMove = pendingMovesRepository.getPendingMove(id);
        log.info("Received pending move with id={}", id);
        return pendingMove;
    }

    public void deletePendingMoveById(int id) {
        pendingMovesRepository.deletePendingMoveById(id);
        log.info("Deleted pending move with id={}", id);
        eventPublisher.publishEvent(
                PendingMoveDeletedEvent.builder()
                        .id(id)
                        .build()
        );
    }

    public void handleFileOperation(int id) {
        PendingMove pendingMove = pendingMovesRepository.getPendingMove(id);

        switch (pendingMove.getOperationType()) {
            case OperationType.MOVE:
                moveFile(pendingMove);
                break;
            case OperationType.TRASH:
                trashFile(pendingMove);
                break;
            case OperationType.NO_MOVE:
                return;
        }

        log.info("Moved file: id={}, fromPath={}, toPath={}", id, pendingMove.getFromPath(), pendingMove.getToPath());
        pendingMovesRepository.deletePendingMoveById(id);
        eventPublisher.publishEvent(
                PendingMoveDeletedEvent.builder()
                        .id(id)
                        .build()
        );
    }

    private void moveFile(PendingMove pendingMove) {
        Path targetPath = pendingMove.getToPath().resolve(pendingMove.getFromPath().getFileName());
        try {
            Files.move(pendingMove.getFromPath(), targetPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void trashFile(PendingMove pendingMove) {
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop is not supported");
        }

        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.MOVE_TO_TRASH)) {
            throw new UnsupportedOperationException("Move to trash is not supported");
        }

        boolean success = desktop.moveToTrash(
                pendingMove.getFromPath().toFile()
        );

        if (!success) {
            throw new RuntimeException("Failed to move file to trash");
        }
    }
}
