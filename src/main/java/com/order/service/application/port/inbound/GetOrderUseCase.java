package com.order.service.application.port.inbound;

import com.order.service.application.dto.OrderResponse;

import java.util.Optional;

public interface GetOrderUseCase {
    Optional<OrderResponse> findById(String id);
}
