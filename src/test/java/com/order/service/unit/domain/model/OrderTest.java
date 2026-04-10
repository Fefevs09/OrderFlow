package com.order.service.unit.domain.model;

import com.order.service.domain.exception.InvalidOrderException;
import com.order.service.domain.model.Customer;
import com.order.service.domain.model.Order;
import com.order.service.domain.model.OrderItem;
import com.order.service.domain.model.vo.Address;
import com.order.service.domain.model.vo.Email;
import com.order.service.domain.model.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

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

    @Test
    @DisplayName("Should create order with valid data")
    void shouldCreateOrderWithValidData() {
        // Arrange
        var customer = createValidCustomer();
        var items = List.of(createValidItem());

        // Act
        var order = Order.create(customer, items);

        // Assert
        assertNotNull(order.getId());
        assertEquals(customer, order.getCustomer());
        assertEquals(1, order.getItems().size());
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {
        // Arrange
        var customer = createValidCustomer();
        var items = List.of(
                OrderItem.create("PROD-001", "Product 1", 2, Money.of(50.00)),
                OrderItem.create("PROD-002", "Product 2", 1, Money.of(100.00))
        );

        // Act
        var order = Order.create(customer, items);

        // Assert
        assertEquals(0, order.getTotalAmount().getAmount().compareTo(Money.of(200.00).getAmount()));
    }

    @Test
    @DisplayName("Should confirm order when pending")
    void shouldConfirmOrderWhenPending() {
        // Arrange
        var customer = createValidCustomer();
        var order = Order.create(customer, List.of(createValidItem()));

        // Act
        order.confirm();

        // Assert
        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when confirming non-pending order")
    void shouldThrowExceptionWhenConfirmingNonPendingOrder() {
        // Arrange
        var customer = createValidCustomer();
        var order = Order.create(customer, List.of(createValidItem()));
        order.confirm();

        // Act & Assert
        assertThrows(InvalidOrderException.class, order::confirm);
    }

    @Test
    @DisplayName("Should deliver order when shipped")
    void shouldDeliverOrderWhenShipped() {
        // Arrange
        var customer = createValidCustomer();
        var order = Order.create(customer, List.of(createValidItem()));
        order.confirm();
        order.ship();

        // Act
        order.deliver();

        // Assert
        assertEquals(Order.OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    @DisplayName("Should return product IDs correctly")
    void shouldReturnProductIdsCorrectly() {
        // Arrange
        var customer = createValidCustomer();
        var items = List.of(
                OrderItem.create("PROD-001", "Product 1", 1, Money.of(50.00)),
                OrderItem.create("PROD-002", "Product 2", 1, Money.of(100.00))
        );
        var order = Order.create(customer, items);

        // Act
        var productIds = order.getProductIds();

        // Assert
        assertEquals(2, productIds.size());
        assertTrue(productIds.contains("PROD-001"));
        assertTrue(productIds.contains("PROD-002"));
    }

    @Test
    @DisplayName("Should return true for isPending when status is pending")
    void shouldReturnTrueForIsPendingWhenStatusIsPending() {
        // Arrange
        var customer = createValidCustomer();
        var order = Order.create(customer, List.of(createValidItem()));

        // Assert
        assertTrue(order.isPending());
    }

    @Test
    @DisplayName("Should return true for isFinalized when status is delivered")
    void shouldReturnTrueForIsFinalizedWhenStatusIsDelivered() {
        // Arrange
        var customer = createValidCustomer();
        var order = Order.create(customer, List.of(createValidItem()));
        order.confirm();
        order.ship();
        order.deliver();

        // Assert
        assertTrue(order.isFinalized());
    }
}
