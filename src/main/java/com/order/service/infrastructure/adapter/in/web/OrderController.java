package com.order.service.infrastructure.adapter.in.web;

import com.order.service.application.dto.OrderRequest;
import com.order.service.application.dto.OrderResponse;
import com.order.service.application.port.inbound.ConfirmOrderUseCase;
import com.order.service.application.port.inbound.CreateOrderUseCase;
import com.order.service.application.port.inbound.GetOrderUseCase;
import com.order.service.application.port.inbound.ListOrdersUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final ConfirmOrderUseCase confirmOrderUseCase;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        var response = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        return getOrderUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(listOrdersUseCase.findByStatus(status));
        }
        return ResponseEntity.ok(listOrdersUseCase.findAll());
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String id) {
        var response = confirmOrderUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}
