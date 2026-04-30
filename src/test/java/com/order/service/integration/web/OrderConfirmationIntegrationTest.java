package com.order.service.integration.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.service.application.dto.ConfirmOrderRequest;
import com.order.service.application.dto.OrderItemRequest;
import com.order.service.application.dto.OrderRequest;
import com.order.service.application.dto.OrderResponse;
import com.order.service.domain.model.Order;
import com.order.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderConfirmationIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private OrderRepository orderRepository;

        private OrderRequest.OrderRequestBuilder createOrderRequestBuilder() {
                var item = OrderItemRequest.builder()
                                .productId("PROD-001")
                                .productName("Product 1")
                                .quantity(2)
                                .unitPrice(new BigDecimal("50.00"))
                                .build();

                return OrderRequest.builder()
                                .customerName("John Doe")
                                .customerEmail("john@example.com")
                                .street("123 Main St")
                                .city("São Paulo")
                                .state("SP")
                                .zipCode("01000-000")
                                .country("BR")
                                .items(List.of(item));
        }

        private String createPendingOrder() throws Exception {
                var request = createOrderRequestBuilder().build();

                MvcResult result = mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andReturn();

                var response = objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                OrderResponse.class);

                return response.getId();
        }

        @Test
        @DisplayName("Should confirm order successfully and return 200 OK")
        void shouldConfirmOrderSuccessfullyAndReturn200Ok() throws Exception {
                // Arrange
                String orderId = createPendingOrder();
                var confirmRequest = ConfirmOrderRequest.builder()
                                .orderId(orderId)
                                .build();

                // Act & Assert
                mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(confirmRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(orderId))
                                .andExpect(jsonPath("$.status").value("CONFIRMED"));

                // Verify order was saved with CONFIRMED status
                Optional<Order> order = orderRepository.findById(orderId);
                assertThat(order).isPresent();
                assertThat(order.get().getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should return 404 when order does not exist")
        void shouldReturn404WhenOrderDoesNotExist() throws Exception {
                // Arrange
                String nonExistentOrderId = "non-existent-order-id";
                var confirmRequest = ConfirmOrderRequest.builder()
                                .orderId(nonExistentOrderId)
                                .build();

                // Act & Assert
                mockMvc.perform(post("/api/v1/orders/{id}/confirm", nonExistentOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(confirmRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Order Not Found"))
                                .andExpect(jsonPath("$.message")
                                                .value("Order not found with id: " + nonExistentOrderId));
        }

        @Test
        @DisplayName("Should return 400 when order is already confirmed")
        void shouldReturn400WhenOrderIsAlreadyConfirmed() throws Exception {
                // Arrange
                String orderId = createPendingOrder();
                var confirmRequest = ConfirmOrderRequest.builder()
                                .orderId(orderId)
                                .build();

                // First confirmation - should succeed
                mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(confirmRequest)))
                                .andExpect(status().isOk());

                // Second confirmation - should fail
                mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(confirmRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Invalid Order"))
                                .andExpect(jsonPath("$.message").value("Only pending orders can be confirmed"));
        }

        @Test
        @DisplayName("Should return 400 when order ID in path does not match body")
        void shouldReturn400WhenOrderIdInPathDoesNotMatchBody() throws Exception {
                // Arrange
                String orderId = createPendingOrder();
                String differentOrderId = "different-order-id";
                var confirmRequest = ConfirmOrderRequest.builder()
                                .orderId(differentOrderId)
                                .build();

                // Act & Assert
                mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(confirmRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation Error"))
                                .andExpect(jsonPath("$.message").value("Order ID in path does not match request body"));
        }

        @Test
        @DisplayName("Should return 400 when request body is invalid")
        void shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
                // Arrange
                String orderId = createPendingOrder();

                // Act & Assert - missing orderId in request
                mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation Error"))
                                .andExpect(jsonPath("$.details[0].field").value("orderId"))
                                .andExpect(jsonPath("$.details[0].message").value("must not be blank"));
        }

        @Test
        @DisplayName("Should update order timestamp when confirming")
        void shouldUpdateOrderTimestampWhenConfirming() throws Exception {
                // Arrange
                String orderId = createPendingOrder();

                Optional<Order> orderBefore = orderRepository.findById(orderId);
                assertThat(orderBefore).isPresent();
                var createdAt = orderBefore.get().getCreatedAt();
                var updatedAtBefore = orderBefore.get().getUpdatedAt();

                var confirmRequest = ConfirmOrderRequest.builder()
                                .orderId(orderId)
                                .build();

                // Act
                Thread.sleep(100); // Ensure timestamp difference

                mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(confirmRequest)))
                                .andExpect(status().isOk());

                // Assert
                Optional<Order> orderAfter = orderRepository.findById(orderId);
                assertThat(orderAfter).isPresent();
                assertThat(orderAfter.get().getUpdatedAt()).isAfter(updatedAtBefore);
                assertThat(orderAfter.get().getCreatedAt()).isEqualTo(createdAt); // CreatedAt should not change
        }
}
