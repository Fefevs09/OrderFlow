package com.order.service.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private final String id;
    private final String customerName;
    private final String customerEmail;
    private final String customerAddress;
    private final List<OrderItemResponse> items;
    private final BigDecimal totalAmount;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
