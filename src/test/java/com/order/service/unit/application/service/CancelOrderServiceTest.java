package com.order.service.unit.application.service;

import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.application.service.CancelOrderService;
import com.order.service.domain.event.OrderStatusUpdatedEvent;
import com.order.service.domain.exception.InvalidOrderException;
import com.order.service.domain.exception.OrderNotFoundException;
import com.order.service.domain.model.Customer;
import com.order.service.domain.model.Order;
import com.order.service.domain.model.OrderItem;
import com.order.service.domain.model.vo.Address;
import com.order.service.domain.model.vo.Email;
import com.order.service.domain.model.vo.Money;
import com.order.service.domain.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private CancelOrderService cancelOrderService;

    private Customer createValidCustomer() {
        return Customer.create(
                "John Doe",
                Email.of("john@example.com"),
                Address.of("123 Main St", "São Paulo", "SP", "01000-000")
        );
    }

    private OrderItem createValidItem() {
        return OrderItem.create("PROD-001", "Product 1", 2, Money.of(50.00));
    }

    private Order createOrderWithStatus(Order.OrderStatus status) {
        var customer = createValidCustomer();
        var items = List.of(createValidItem());
        return Order.of(
                UUID.randomUUID().toString(),
                customer,
                items,
                status,
                null,
                null);
    }

    @Test
    @DisplayName("Should cancel pending order successfully")
    void shouldCancelPendingOrderSuccessfully() {
        // Arrange
        var order = createOrderWithStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = cancelOrderService.execute(order.getId());

        // Assert
        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
        assertEquals("CANCELLED", response.getStatus());
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).findById(order.getId());
        verify(orderRepository).save(order);
        verify(eventPublisherPort).publish(any(OrderStatusUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should cancel confirmed order successfully")
    void shouldCancelConfirmedOrderSuccessfully() {
        // Arrange
        var order = createOrderWithStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = cancelOrderService.execute(order.getId());

        // Assert
        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should cancel processing order successfully")
    void shouldCancelProcessingOrderSuccessfully() {
        // Arrange
        var order = createOrderWithStatus(Order.OrderStatus.PROCESSING);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = cancelOrderService.execute(order.getId());

        // Assert
        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should publish event with correct statuses when cancelling")
    void shouldPublishEventWithCorrectStatusesWhenCancelling() {
        // Arrange
        var order = createOrderWithStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cancelOrderService.execute(order.getId());

        // Assert
        var eventCaptor = ArgumentCaptor.forClass(OrderStatusUpdatedEvent.class);
        verify(eventPublisherPort).publish(eventCaptor.capture());
        var event = eventCaptor.getValue();
        assertEquals(Order.OrderStatus.PENDING, event.getOldStatus());
        assertEquals(Order.OrderStatus.CANCELLED, event.getNewStatus());
    }

    @Test
    @DisplayName("Should throw exception when order is not found")
    void shouldThrowExceptionWhenOrderIsNotFound() {
        // Arrange
        var orderId = UUID.randomUUID().toString();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> cancelOrderService.execute(orderId));
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw exception when order is shipped")
    void shouldThrowExceptionWhenOrderIsShipped() {
        // Arrange
        var order = createOrderWithStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> cancelOrderService.execute(order.getId()));
        verify(orderRepository).findById(order.getId());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when order is delivered")
    void shouldThrowExceptionWhenOrderIsDelivered() {
        // Arrange
        var order = createOrderWithStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> cancelOrderService.execute(order.getId()));
        verify(orderRepository).findById(order.getId());
        verify(orderRepository, never()).save(any());
    }
}
