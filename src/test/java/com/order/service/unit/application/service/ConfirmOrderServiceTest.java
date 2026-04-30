package com.order.service.unit.application.service;

import com.order.service.application.port.outbound.EventPublisherPort;
import com.order.service.application.service.ConfirmOrderService;
import com.order.service.domain.exception.InvalidOrderException;
import com.order.service.domain.exception.OrderNotFoundException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderServiceTest {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private EventPublisherPort eventPublisherPort;

        @InjectMocks
        private ConfirmOrderService confirmOrderService;

        private Customer createValidCustomer() {
                return Customer.create(
                                "John Doe",
                                Email.of("john@example.com"),
                                Address.of("123 Main St", "São Paulo", "SP", "01000-000"));
        }

        private OrderItem createValidItem() {
                return OrderItem.create("PROD-001", "Product 1", 2, Money.of(50.00));
        }

        private Order createPendingOrder() {
                var customer = createValidCustomer();
                var items = List.of(createValidItem());
                var order = Order.create(customer, items);
                return Order.of(
                                UUID.randomUUID().toString(),
                                order.getCustomer(),
                                order.getItems(),
                                order.getStatus(),
                                order.getCreatedAt(),
                                order.getUpdatedAt());
        }

        private Order createConfirmedOrder() {
                var customer = createValidCustomer();
                var items = List.of(createValidItem());
                var order = Order.create(customer, items);
                order.confirm();
                return Order.of(
                                UUID.randomUUID().toString(),
                                order.getCustomer(),
                                order.getItems(),
                                order.getStatus(),
                                order.getCreatedAt(),
                                order.getUpdatedAt());
        }

        @Test
        @DisplayName("Should confirm order successfully when order is pending")
        void shouldConfirmOrderSuccessfullyWhenOrderIsPending() {
                // Arrange
                var order = createPendingOrder();

                when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
                when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                var response = confirmOrderService.execute(order.getId());

                // Assert
                assertNotNull(response);
                assertEquals(order.getId(), response.getId());
                assertEquals("CONFIRMED", response.getStatus());
                assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
                verify(orderRepository, times(1)).findById(order.getId());
                verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("Should publish event when confirming order")
        void shouldPublishEventWhenConfirmingOrder() {
                // Arrange
                var order = createPendingOrder();

                when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
                when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                confirmOrderService.execute(order.getId());

                // Assert
                var eventCaptor = org.mockito.ArgumentCaptor.forClass(com.order.service.domain.event.OrderStatusUpdatedEvent.class);
                verify(eventPublisherPort, times(1)).publish(eventCaptor.capture());
                var event = eventCaptor.getValue();
                assertEquals(Order.OrderStatus.PENDING, event.getOldStatus());
                assertEquals(Order.OrderStatus.CONFIRMED, event.getNewStatus());
        }

        @Test
        @DisplayName("Should throw exception when order is not found")
        void shouldThrowExceptionWhenOrderIsNotFound() {
                // Arrange
                var orderId = UUID.randomUUID().toString();

                when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(OrderNotFoundException.class, () -> confirmOrderService.execute(orderId));
                verify(orderRepository, times(1)).findById(orderId);
                verify(orderRepository, never()).save(any());
                verify(eventPublisherPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should throw exception when order is already confirmed")
        void shouldThrowExceptionWhenOrderIsAlreadyConfirmed() {
                // Arrange
                var order = createConfirmedOrder();

                when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

                // Act & Assert
                assertThrows(InvalidOrderException.class, () -> confirmOrderService.execute(order.getId()));
                verify(orderRepository, times(1)).findById(order.getId());
                verify(orderRepository, never()).save(any());
                verify(eventPublisherPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should throw exception when order is cancelled")
        void shouldThrowExceptionWhenOrderIsCancelled() {
                // Arrange
                var order = createPendingOrder();
                order.cancel();
                var cancelledOrder = Order.of(
                                order.getId(),
                                order.getCustomer(),
                                order.getItems(),
                                order.getStatus(),
                                order.getCreatedAt(),
                                order.getUpdatedAt());

                when(orderRepository.findById(cancelledOrder.getId())).thenReturn(Optional.of(cancelledOrder));

                // Act & Assert
                assertThrows(InvalidOrderException.class, () -> confirmOrderService.execute(cancelledOrder.getId()));
                verify(orderRepository, times(1)).findById(cancelledOrder.getId());
                verify(orderRepository, never()).save(any());
                verify(eventPublisherPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should throw exception when order is shipped")
        void shouldThrowExceptionWhenOrderIsShipped() {
                // Arrange
                var order = createConfirmedOrder();
                order.ship();
                var shippedOrder = Order.of(
                                order.getId(),
                                order.getCustomer(),
                                order.getItems(),
                                order.getStatus(),
                                order.getCreatedAt(),
                                order.getUpdatedAt());

                when(orderRepository.findById(shippedOrder.getId())).thenReturn(Optional.of(shippedOrder));

                // Act & Assert
                assertThrows(InvalidOrderException.class, () -> confirmOrderService.execute(shippedOrder.getId()));
                verify(orderRepository, times(1)).findById(shippedOrder.getId());
                verify(orderRepository, never()).save(any());
                verify(eventPublisherPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should throw exception when order is delivered")
        void shouldThrowExceptionWhenOrderIsDelivered() {
                // Arrange
                var order = createConfirmedOrder();
                order.ship();
                order.deliver();
                var deliveredOrder = Order.of(
                                order.getId(),
                                order.getCustomer(),
                                order.getItems(),
                                order.getStatus(),
                                order.getCreatedAt(),
                                order.getUpdatedAt());

                when(orderRepository.findById(deliveredOrder.getId())).thenReturn(Optional.of(deliveredOrder));

                // Act & Assert
                assertThrows(InvalidOrderException.class, () -> confirmOrderService.execute(deliveredOrder.getId()));
                verify(orderRepository, times(1)).findById(deliveredOrder.getId());
                verify(orderRepository, never()).save(any());
                verify(eventPublisherPort, never()).publish(any());
        }
}
