package com.order.service.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfirmOrderRequest {
    @NotBlank(message = "")
    private final String orderId;
}
