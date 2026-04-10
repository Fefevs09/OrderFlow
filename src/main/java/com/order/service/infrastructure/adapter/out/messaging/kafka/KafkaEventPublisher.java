package com.order.service.infrastructure.adapter.out.messaging.kafka;

import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.orders:orders}")
    private String ordersTopic;

    @Override
    public void publish(DomainEvent event) {
        log.info("Publishing event {} to Kafka topic {}", event.getEventType(), ordersTopic);
        kafkaTemplate.send(ordersTopic, event.getAggregateId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {}: {}", event.getEventId(), ex.getMessage(), ex);
                    } else {
                        log.debug("Event {} published successfully to partition {} offset {}",
                                event.getEventId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
