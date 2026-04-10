package com.order.service.application.port.inbound;

import com.order.service.application.dto.OrderResponse;

import java.util.List;

public interface ListOrdersUseCase {
    List<OrderResponse> findAll();
    List<OrderResponse> findByStatus(String status);
}
