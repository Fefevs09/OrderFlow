package com.order.service.application.port.outbound;

import com.order.service.domain.model.OrderItem;

import java.util.List;
import java.util.Map;

public interface StockServicePort {
    boolean checkAvailability(String productId, int quantity);
    Map<String, Integer> checkAvailabilityBatch(List<OrderItem> items);
    void reserveStock(String orderId, List<OrderItem> items);
    void releaseStock(String orderId, List<OrderItem> items);
}
