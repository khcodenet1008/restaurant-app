package com.example.restaurant.kitchen.web;

import java.time.LocalDateTime;
import java.util.List;

public record KitchenTicketResponse(
        String id,
        String orderId,
        String sagaId,
        String queueName,
        String status,
        int priority,
        String assignedTo,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt,
        LocalDateTime completedAt,
        List<KitchenTicketItemResponse> items) {
}
