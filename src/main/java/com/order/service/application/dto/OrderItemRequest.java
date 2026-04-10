package com.order.service.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemRequest {
    @NotBlank(message = "Product ID is required")
    private final String productId;

    @NotBlank(message = "Product name is required")
    private final String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private final int quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private final BigDecimal unitPrice;
}
