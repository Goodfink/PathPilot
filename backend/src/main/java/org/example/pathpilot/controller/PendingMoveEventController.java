package org.example.pathpilot.controller;

import lombok.RequiredArgsConstructor;
import org.example.pathpilot.service.event.PendingMoveSSEService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@CrossOrigin(origins = "http://localhost:1420")
@RestController
@RequestMapping("api/events")
@RequiredArgsConstructor
public class PendingMoveEventController {

    private final PendingMoveSSEService pendingMoveSSEService;

    @GetMapping(value = "/pending-moves", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return pendingMoveSSEService.subscribe();
    }
}
