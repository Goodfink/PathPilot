package org.example.pathpilot.model.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingMoveDeletedEvent {
    private int id;
}
