package com.order.service.application.port.outbound;

import com.order.service.domain.event.DomainEvent;

public interface EventPublisherPort {
    void publish(DomainEvent event);
}
