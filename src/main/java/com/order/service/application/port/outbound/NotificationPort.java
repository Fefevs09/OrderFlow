package com.order.service.application.port.outbound;

import com.order.service.domain.model.Order;

public interface NotificationPort {
    void notifyOrderCreated(Order order);
    void notifyOrderStatusUpdated(Order order, String message);
}
