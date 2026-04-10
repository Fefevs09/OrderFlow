package com.order.service.domain.model;

import com.order.service.domain.exception.InvalidOrderException;
import com.order.service.domain.model.vo.Money;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Order {
    private final String id;
    private final Customer customer;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    private Order(String id, Customer customer, List<OrderItem> items, OrderStatus status,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.customer = Objects.requireNonNull(customer, "Customer cannot be null");
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.status = status != null ? status : OrderStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;

        validate();
    }

    public static Order create(Customer customer, List<OrderItem> items) {
        return new Order(null, customer, items, OrderStatus.PENDING, null, null);
    }

    public static Order of(String id, Customer customer, List<OrderItem> items,
                           OrderStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Order(id, customer, items, status, createdAt, updatedAt);
    }

    private void validate() {
        if (items.isEmpty()) {
            throw new InvalidOrderException("Order must have at least one item");
        }
    }

    public Money getTotalAmount() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money::add)
                .orElse(Money.zero());
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(OrderItem item) {
        items.add(Objects.requireNonNull(item));
        updateTimestamp();
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
        updateTimestamp();
    }

    public void ship() {
        if (status != OrderStatus.CONFIRMED && status != OrderStatus.PROCESSING) {
            throw new InvalidOrderException("Only confirmed or processing orders can be shipped");
        }
        this.status = OrderStatus.SHIPPED;
        updateTimestamp();
    }

    public void deliver() {
        if (status != OrderStatus.SHIPPED) {
            throw new InvalidOrderException("Only shipped orders can be delivered");
        }
        this.status = OrderStatus.DELIVERED;
        updateTimestamp();
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new InvalidOrderException("Shipped or delivered orders cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        updateTimestamp();
    }

    public void process() {
        if (status != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException("Only confirmed orders can be processed");
        }
        this.status = OrderStatus.PROCESSING;
        updateTimestamp();
    }

    public List<String> getProductIds() {
        return items.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMED;
    }

    public boolean isFinalized() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED;
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
