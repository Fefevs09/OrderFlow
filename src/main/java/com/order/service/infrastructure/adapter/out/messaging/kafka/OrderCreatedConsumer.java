package com.order.service.infrastructure.adapter.out.messaging.kafka;

import com.order.service.application.port.outbound.NotificationPort;
import com.order.service.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final NotificationPort notificationPort;

    @KafkaListener(topics = "${kafka.topic.orders:orders}", groupId = "${spring.application.name:service}")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order: {}", event.getOrder().getId());
        try {
            notificationPort.notifyOrderCreated(event.getOrder());
            log.info("Successfully processed OrderCreatedEvent for order: {}", event.getOrder().getId());
        } catch (Exception e) {
            log.error("Failed to process OrderCreatedEvent for order: {}", event.getOrder().getId(), e);
            throw e;
        }
    }
}
