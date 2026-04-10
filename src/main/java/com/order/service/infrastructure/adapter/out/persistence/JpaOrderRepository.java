package com.order.service.infrastructure.adapter.out.persistence;

import com.order.service.domain.model.Order;
import com.order.service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springDataRepository;
    private final OrderJpaMapper mapper;

    @Override
    @Transactional
    public Order save(Order order) {
        var entity = mapper.toEntity(order);
        var saved = springDataRepository.save(entity);
        // Force flush and reload to ensure all data is persisted
        springDataRepository.flush();
        // Re-fetch to ensure we have the complete saved entity
        var reloaded = springDataRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("Failed to reload saved order: " + saved.getId()));
        return mapper.toDomain(reloaded);
    }

    @Override
    public Optional<Order> findById(String id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(Order.OrderStatus status) {
        return springDataRepository.findByStatus(OrderJpaEntity.OrderStatus.valueOf(status.name())).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return springDataRepository.existsById(id);
    }
}
