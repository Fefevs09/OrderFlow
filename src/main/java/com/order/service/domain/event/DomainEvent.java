package com.order.service.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    String getEventId();
    String getAggregateId();
    LocalDateTime getOccurredAt();
    String getEventType();
}
