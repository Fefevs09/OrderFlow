package com.order.service.domain.event;

import com.order.service.domain.model.Order;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OrderStatusUpdatedEvent implements DomainEvent {
    private final String eventId;
    private final String aggregateId;
    private final LocalDateTime occurredAt;
    private final String eventType;
    private final Order.OrderStatus oldStatus;
    private final Order.OrderStatus newStatus;
    private final String orderId;

    public OrderStatusUpdatedEvent(Order order, Order.OrderStatus oldStatus) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = order.getId();
        this.occurredAt = LocalDateTime.now();
        this.eventType = "ORDER_STATUS_UPDATED";
        this.oldStatus = oldStatus;
        this.newStatus = order.getStatus();
        this.orderId = order.getId();
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
