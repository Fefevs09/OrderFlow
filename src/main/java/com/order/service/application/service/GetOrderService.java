package com.order.service.application.service;

import com.order.service.application.dto.OrderItemResponse;
import com.order.service.application.dto.OrderResponse;
import com.order.service.application.port.inbound.GetOrderUseCase;
import com.order.service.domain.model.Order;
import com.order.service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    public Optional<OrderResponse> findById(String id) {
        return orderRepository.findById(id)
                .map(this::mapToResponse);
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
