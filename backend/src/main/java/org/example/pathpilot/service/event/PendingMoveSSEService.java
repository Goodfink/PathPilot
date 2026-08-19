package org.example.pathpilot.service.event;

import lombok.NoArgsConstructor;
import org.example.pathpilot.model.event.PendingMoveCreatedEvent;
import org.example.pathpilot.model.event.PendingMoveDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@NoArgsConstructor
public class PendingMoveSSEService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(PendingMoveSSEService.class);


    public SseEmitter subscribe() {
        log.info("SSE client connected");

        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));

        return emitter;
    }

    @EventListener
    public void handlePendingMoveCreated(PendingMoveCreatedEvent event) {
        log.info("Pending move event received: emitters = {}", emitters.size());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("pending-move-created")
                        .data(event.getId()));

            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
                throw new RuntimeException(e);
            }
        }
    }

    @EventListener
    public void handlePendingMoveDeleted(PendingMoveDeletedEvent event) {
        log.info("Pending move event received: emitters = {}", emitters.size());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("pending-move-deleted")
                        .data(event.getId()));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
                throw new RuntimeException(e);
            }
        }
    }
}
