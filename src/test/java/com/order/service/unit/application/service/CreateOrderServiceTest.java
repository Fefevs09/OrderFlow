package com.order.service.unit.application.service;

import com.order.service.application.dto.OrderItemRequest;
import com.order.service.application.dto.OrderRequest;
import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.application.service.CreateOrderService;
import com.order.service.domain.event.OrderCreatedEvent;
import com.order.service.domain.model.Customer;
import com.order.service.domain.model.Order;
import com.order.service.domain.model.OrderItem;
import com.order.service.domain.model.vo.Address;
import com.order.service.domain.model.vo.Email;
import com.order.service.domain.model.vo.Money;
import com.order.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private CreateOrderService createOrderService;

    private OrderRequest createValidRequest() {
        var item = OrderItemRequest.builder()
                .productId("PROD-001")
                .productName("Product 1")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        return OrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .street("123 Main St")
                .city("São Paulo")
                .state("SP")
                .zipCode("01000-000")
                .country("BR")
                .items(List.of(item))
                .build();
    }

    private Order createValidOrder() {
        var customer = Customer.create(
                "John Doe",
                Email.of("john@example.com"),
                Address.of("123 Main St", "São Paulo", "SP", "01000-000"));
        var items = List.of(OrderItem.create("PROD-001", "Product 1", 2, Money.of(50.00)));
        return Order.create(customer, items);
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Arrange
        var request = createValidRequest();
        var orderId = UUID.randomUUID().toString();
        // var order = createValidOrder();

        // Make repository return the saved order with an ID
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            // Return the order with a fixed ID
            return Order.of(orderId, savedOrder.getCustomer(), savedOrder.getItems(),
                    savedOrder.getStatus(), savedOrder.getCreatedAt(), savedOrder.getUpdatedAt());
        });

        // Act
        var response = createOrderService.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("john@example.com", response.getCustomerEmail());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should publish OrderCreatedEvent when creating order")
    void shouldPublishOrderCreatedEventWhenCreatingOrder() {
        // Arrange
        var request = createValidRequest();

        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            return Order.of(UUID.randomUUID().toString(), savedOrder.getCustomer(), savedOrder.getItems(),
                    savedOrder.getStatus(), savedOrder.getCreatedAt(), savedOrder.getUpdatedAt());
        });

        // Act
        createOrderService.execute(request);

        // Assert
        var eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        assertNotNull(eventCaptor.getValue());
        assertNotNull(eventCaptor.getValue().getOrder());
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {
        // Arrange
        var items = List.of(
                OrderItemRequest.builder()
                        .productId("PROD-001")
                        .productName("Product 1")
                        .quantity(2)
                        .unitPrice(new BigDecimal("50.00"))
                        .build(),
                OrderItemRequest.builder()
                        .productId("PROD-002")
                        .productName("Product 2")
                        .quantity(1)
                        .unitPrice(new BigDecimal("100.00"))
                        .build());
        var request = OrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .street("123 Main St")
                .city("São Paulo")
                .state("SP")
                .zipCode("01000-000")
                .items(items)
                .build();

        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            return Order.of(UUID.randomUUID().toString(), savedOrder.getCustomer(), savedOrder.getItems(),
                    savedOrder.getStatus(), savedOrder.getCreatedAt(), savedOrder.getUpdatedAt());
        });

        // Act
        var response = createOrderService.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("200.00"), response.getTotalAmount());
        verify(orderRepository, times(1)).save(any());
    }
}
