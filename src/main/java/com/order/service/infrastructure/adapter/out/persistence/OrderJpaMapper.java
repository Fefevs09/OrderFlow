package com.order.service.infrastructure.adapter.out.persistence;

import com.order.service.domain.model.Customer;
import com.order.service.domain.model.Order;
import com.order.service.domain.model.OrderItem;
import com.order.service.domain.model.vo.Address;
import com.order.service.domain.model.vo.Email;
import com.order.service.domain.model.vo.Money;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderJpaMapper {

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        // Validate customer fields
        if (entity.getCustomerName() == null || entity.getCustomerEmail() == null) {
            throw new IllegalStateException("Order entity has null customer fields: id=" + entity.getId());
        }

        var customer = Customer.of(
                null,
                entity.getCustomerName(),
                Email.of(entity.getCustomerEmail()),
                Address.of(
                        entity.getStreet() != null ? entity.getStreet() : "",
                        entity.getCity() != null ? entity.getCity() : "",
                        entity.getState(),
                        entity.getZipCode() != null ? entity.getZipCode() : "",
                        entity.getCountry() != null ? entity.getCountry() : "BR"
                )
        );

        var items = entity.getItems() != null ? entity.getItems().stream()
                .map(this::toDomainItem)
                .collect(Collectors.toList()) : new ArrayList<OrderItem>();

        return Order.of(
                entity.getId(),
                customer,
                items,
                Order.OrderStatus.valueOf(entity.getStatus().name()),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : java.time.LocalDateTime.now(),
                entity.getUpdatedAt()
        );
    }

    public OrderItem toDomainItem(OrderItemJpaEntity entity) {
        return OrderItem.of(
                entity.getId(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getQuantity(),
                Money.of(entity.getUnitPrice())
        );
    }

    public OrderJpaEntity toEntity(Order order) {
        var entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setCustomerName(order.getCustomer().getName());
        entity.setCustomerEmail(order.getCustomer().getEmailValue());
        entity.setStreet(order.getCustomer().getAddress().getStreet());
        entity.setCity(order.getCustomer().getAddress().getCity());
        entity.setState(order.getCustomer().getAddress().getState());
        entity.setZipCode(order.getCustomer().getAddress().getZipCode());
        entity.setCountry(order.getCustomer().getAddress().getCountry());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setStatus(OrderJpaEntity.OrderStatus.valueOf(order.getStatus().name()));
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
                .map(item -> toEntityItem(item, order.getId()))
                .collect(Collectors.toList());
        entity.setItems(itemEntities);

        return entity;
    }

    public OrderItemJpaEntity toEntityItem(OrderItem item, String orderId) {
        var entity = new OrderItemJpaEntity();
        entity.setId(item.getId());
        entity.setOrderId(orderId);
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice().getAmount());
        entity.setTotalPrice(item.getTotalPrice().getAmount());
        return entity;
    }
}
