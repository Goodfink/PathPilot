package org.example.pathpilot.controller;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.model.pendingMove.PendingMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.pathpilot.service.pendingMoves.PendingMoveService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:1420")
@RestController
@RequestMapping("api/pending-moves")
@RequiredArgsConstructor
public class PendingMoveController {

    private static final Logger log = LoggerFactory.getLogger(PendingMoveController.class);

    private final PendingMoveService pendingMoveService;

    @GetMapping
    public List<PendingMove> getPendingMoves() {
        log.info("Getting pending moves");
        List<PendingMove> pendingMoves = pendingMoveService.getPendingMoves();
        return pendingMoves;
    }

    @GetMapping("/{id}")
    public PendingMove getPendingMoveById(@PathVariable int id) {
        log.info("Getting pending move by id: id={}", id);
        return pendingMoveService.getPendingMoveById(id);
    }

    @DeleteMapping("/{id}")
    public void deletePendingMoveById(@PathVariable int id) {
        log.info("Deleting pending move by id: id={}", id);
        pendingMoveService.deletePendingMoveById(id);
    }

    @PostMapping("/{id}/approve")
    public void approvePendingMove(@PathVariable int id) {
        log.info("Moving file with id: id={}", id);
        pendingMoveService.handleFileOperation(id);
    }
}
