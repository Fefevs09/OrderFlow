package com.order.service.infrastructure.adapter.out.messaging.rabbitmq;

import com.order.service.application.port.outbound.NotificationPort;
import com.order.service.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbitmq")
public class OrderCreatedListener {

    private final NotificationPort notificationPort;

    @RabbitListener(queues = "${rabbitmq.queue.orders:orders.queue}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent from RabbitMQ for order: {}", event.getOrder().getId());
        try {
            notificationPort.notifyOrderCreated(event.getOrder());
            log.info("Successfully processed OrderCreatedEvent from RabbitMQ for order: {}", event.getOrder().getId());
        } catch (Exception e) {
            log.error("Failed to process OrderCreatedEvent from RabbitMQ for order: {}", event.getOrder().getId(), e);
            throw e;
        }
    }
}
