package com.order.service.domain.event;

import com.order.service.domain.model.Order;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OrderCreatedEvent implements DomainEvent {
    private final String eventId;
    private final String aggregateId;
    private final LocalDateTime occurredAt;
    private final String eventType;
    private final Order order;

    public OrderCreatedEvent(Order order) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = order.getId();
        this.occurredAt = LocalDateTime.now();
        this.eventType = "ORDER_CREATED";
        this.order = order;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getEventType() {
        return eventType;
    }
}
