package com.order.service.application.service;

import com.order.service.application.dto.OrderItemResponse;
import com.order.service.application.dto.OrderResponse;
import com.order.service.application.port.inbound.ListOrdersUseCase;
import com.order.service.domain.model.Order;
import com.order.service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Cacheable(value = "orders")
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "ordersByStatus", key = "#status")
    public List<OrderResponse> findByStatus(String status) {
        var orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
