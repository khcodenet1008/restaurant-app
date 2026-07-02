package com.example.restaurant.order.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String customerId,
        String sagaId,
        String status,
        String paymentMethod,
        BigDecimal subtotalAmount,
        String currency,
        String cancelReason,
        LocalDateTime createdAt,
        List<OrderItemResponse> items) {
}
