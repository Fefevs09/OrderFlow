package com.order.service.application.service;

import com.order.service.application.dto.OrderItemResponse;
import com.order.service.application.dto.OrderResponse;
import com.order.service.application.port.inbound.ConfirmOrderUseCase;
import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.domain.event.OrderStatusUpdatedEvent;
import com.order.service.domain.exception.OrderNotFoundException;
import com.order.service.domain.model.Order;
import com.order.service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfirmOrderService implements ConfirmOrderUseCase {

    private final OrderRepository orderRepository;
    private final EventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    @CacheEvict(value = { "orders", "ordersByStatus" }, allEntries = true)
    public OrderResponse execute(String id) {
        return orderRepository.findById(id)
                .map(this::confirmOrder)
                .map(this::saveAndPublish)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private Order confirmOrder(Order order) {
        order.confirm();
        return order;
    }

    private Order saveAndPublish(Order order) {
        var previousStatus = order.getStatus();

        orderRepository.save(order);
        eventPublisherPort.publish(new OrderStatusUpdatedEvent(order, previousStatus));
        return order;
    }

    private OrderResponse mapToResponse(Order order) {
        var itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice().getAmount())
                        .totalPrice(item.getTotalPrice().getAmount())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomer().getName())
                .customerEmail(order.getCustomer().getEmailValue())
                .customerAddress(order.getCustomer().getAddress().toString())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount().getAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

}
