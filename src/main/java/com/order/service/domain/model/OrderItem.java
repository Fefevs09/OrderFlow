package com.order.service.domain.model;

import com.order.service.domain.exception.InvalidOrderException;
import com.order.service.domain.model.vo.Money;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class OrderItem {
    private final String id;
    private final String productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;

    private OrderItem(String id, String productId, String productName, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new InvalidOrderException("Quantity must be greater than zero");
        }
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        this.productName = Objects.requireNonNull(productName, "Product name cannot be null");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");
    }

    public static OrderItem create(String productId, String productName, int quantity, Money unitPrice) {
        return new OrderItem(null, productId, productName, quantity, unitPrice);
    }

    public static OrderItem of(String id, String productId, String productName, int quantity, Money unitPrice) {
        return new OrderItem(id, productId, productName, quantity, unitPrice);
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(quantity);
    }

    public OrderItem withQuantity(int newQuantity) {
        return new OrderItem(this.id, this.productId, this.productName, newQuantity, this.unitPrice);
    }
}
