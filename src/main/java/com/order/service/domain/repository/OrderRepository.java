package com.order.service.domain.repository;

import com.order.service.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    List<Order> findAll();
    List<Order> findByStatus(Order.OrderStatus status);
    void deleteById(String id);
    boolean existsById(String id);
}
