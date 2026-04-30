package com.order.service.infrastructure.adapter.out.notification;

import com.order.service.application.port.outbound.NotificationPort;
import com.order.service.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class SesEmailAdapter implements NotificationPort {

    private final SesClient sesClient;

    private static final String FROM_EMAIL = "orders@service.com";

    @Override
    public void notifyOrderCreated(Order order) {
        String subject = String.format("Pedido %s confirmado", order.getId());
        String body = String.format("""
                Olá %s,

                Seu pedido foi criado com sucesso!

                Número do Pedido: %s
                Total: R$ %s
                Status: %s

                Itens do pedido:
                %s

                Obrigado pela preferência!
                """,
                order.getCustomer().getName(),
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                buildItemsList(order));

        sendEmail(order.getCustomer().getEmailValue(), subject, body);
    }

    @Override
    public void notifyOrderStatusUpdated(Order order, String message) {
        String subject = String.format("Atualização do Pedido %s", order.getId());
        String body = String.format("""
                Olá %s,

                Seu pedido teve uma atualização de status.

                Número do Pedido: %s
                Novo Status: %s
                %s

                """,
                order.getCustomer().getName(),
                order.getId(),
                order.getStatus(),
                message);

        sendEmail(order.getCustomer().getEmailValue(), subject, body);
    }

    @Override
    public void sendOrderConfirmationEmail(Order order) {

    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder()
                            .toAddresses(to)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data(subject)
                                    .build())
                            .body(Body.builder()
                                    .text(Content.builder()
                                            .data(body)
                                            .build())
                                    .build())
                            .build())
                    .source(FROM_EMAIL)
                    .build();

            sesClient.sendEmail(request);
            log.info("Email sent to {} via SES", to);
        } catch (SesException e) {
            log.error("Failed to send email via SES: {}", e.getMessage(), e);
        }
    }

    private String buildItemsList(Order order) {
        StringBuilder sb = new StringBuilder();
        order.getItems().forEach(item ->
                sb.append(String.format("- %s (x%d) - R$ %s%n",
                        item.getProductName(),
                        item.getQuantity(),
                        item.getTotalPrice()))
        );
        return sb.toString();
    }
}
