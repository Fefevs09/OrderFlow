package com.order.service.infrastructure.adapter.out.messaging.rabbitmq;

import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbitmq")
public class RabbitEventPublisher implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.orders:orders.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key.orders:orders.routing.key}")
    private String routingKey;

    @Override
    public void publish(DomainEvent event) {
        log.info("Publishing event {} to RabbitMQ exchange {}", event.getEventType(), exchange);
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.debug("Event {} published successfully to RabbitMQ", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event {}: {}", event.getEventId(), e.getMessage(), e);
            throw e;
        }
    }
}
