package com.order.service.infrastructure.adapter.out.notification;

import com.order.service.application.port.outbound.NotificationPort;
import com.order.service.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnsNotificationAdapter implements NotificationPort {

    private final SnsClient snsClient;

    @Value("${aws.sns.topic.arn}")
    private String topicArn;

    @Override
    public void notifyOrderCreated(Order order) {
        String message = String.format("Novo pedido criado: %s\nCliente: %s\nTotal: %s",
                order.getId(),
                order.getCustomer().getName(),
                order.getTotalAmount());

        publishToSns("Novo Pedido", message);
    }

    @Override
    public void notifyOrderStatusUpdated(Order order, String statusMessage) {
        String message = String.format("Status do pedido %s atualizado para: %s",
                order.getId(),
                order.getStatus());

        publishToSns("Atualização de Pedido", message);
    }

    private void publishToSns(String subject, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(message)
                    .build();

            snsClient.publish(request);
            log.info("Notification sent via SNS: {}", subject);
        } catch (Exception e) {
            log.error("Failed to send notification via SNS: {}", e.getMessage(), e);
        }
    }
}
