package org.example.pathpilot.model.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.example.pathpilot.model.pendingMove.PendingMove;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingMoveCreatedEvent {
    private long id;
    private PendingMove pendingMove;
}
