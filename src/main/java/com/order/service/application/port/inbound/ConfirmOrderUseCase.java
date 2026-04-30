package com.order.service.application.port.inbound;

import com.order.service.application.dto.OrderResponse;

public interface ConfirmOrderUseCase {
    OrderResponse execute(String id);
}
