package com.order.service.application.port.inbound;

import com.order.service.application.dto.OrderResponse;

public interface CancelOrderUseCase {
  OrderResponse execute(String id);
}
