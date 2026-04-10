package com.order.service.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, name = "order_id")
    private String orderId;

    @Column(nullable = false, name = "product_id")
    private String productId;

    @Column(nullable = false, name = "product_name")
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2, name = "unit_price")
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2, name = "total_price")
    private BigDecimal totalPrice;
}
