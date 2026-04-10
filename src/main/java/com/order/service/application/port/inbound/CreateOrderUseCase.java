package com.order.service.application.port.inbound;

import com.order.service.application.dto.OrderRequest;
import com.order.service.application.dto.OrderResponse;

public interface CreateOrderUseCase {
    OrderResponse execute(OrderRequest request);
}
