package com.order.service.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderRequest {
    @NotBlank(message = "Customer name is required")
    private final String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private final String customerEmail;

    @NotBlank(message = "Street is required")
    private final String street;

    @NotBlank(message = "City is required")
    private final String city;

    @NotBlank(message = "State is required")
    private final String state;

    @NotBlank(message = "Zip code is required")
    private final String zipCode;

    private final String country;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private final List<OrderItemRequest> items;
}
