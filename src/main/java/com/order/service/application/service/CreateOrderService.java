package com.order.service.application.service;

import com.order.service.application.dto.OrderItemResponse;
import com.order.service.application.dto.OrderRequest;
import com.order.service.application.dto.OrderResponse;
import com.order.service.application.port.inbound.CreateOrderUseCase;
import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.domain.event.OrderCreatedEvent;
import com.order.service.domain.model.Customer;
import com.order.service.domain.model.Order;
import com.order.service.domain.model.OrderItem;
import com.order.service.domain.model.vo.Address;
import com.order.service.domain.model.vo.Email;
import com.order.service.domain.model.vo.Money;
import com.order.service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final EventPublisherPort eventPublisher;

    @Override
    @Transactional
    public OrderResponse execute(OrderRequest request) {
        var customer = createCustomer(request);
        var items = createOrderItems(request);
        var order = Order.create(customer, items);

        var savedOrder = orderRepository.save(order);
        eventPublisher.publish(new OrderCreatedEvent(savedOrder));

        return mapToResponse(savedOrder);
    }

    private Customer createCustomer(OrderRequest request) {
        return Customer.create(
                request.getCustomerName(),
                Email.of(request.getCustomerEmail()),
                Address.of(
                        request.getStreet(),
                        request.getCity(),
                        request.getState(),
                        request.getZipCode(),
                        request.getCountry()
                )
        );
    }

    private List<OrderItem> createOrderItems(OrderRequest request) {
        return request.getItems().stream()
                .map(item -> OrderItem.create(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        Money.of(item.getUnitPrice())
                ))
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
