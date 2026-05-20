package com.order.service.application.service;

import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.order.service.application.dto.OrderItemResponse;
import com.order.service.application.dto.OrderResponse;
import com.order.service.application.port.inbound.CancelOrderUseCase;
import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.domain.event.OrderStatusUpdatedEvent;
import com.order.service.domain.exception.OrderNotFoundException;
import com.order.service.domain.model.Order;
import com.order.service.domain.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

  private final OrderRepository orderRepository;
  private final EventPublisherPort eventPublisherPort;

  @Override
  @Transactional
  @CacheEvict(value = { "orders", "ordersByStatus" }, allEntries = true)
  public OrderResponse execute(String id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));

    var previousStatus = order.getStatus();
    order.cancel();

    orderRepository.save(order);
    eventPublisherPort.publish(new OrderStatusUpdatedEvent(order, previousStatus));

    return mapToResponse(order);
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
