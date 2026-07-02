package com.example.restaurant.kitchen.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateKitchenTicketStatusRequest(
        @NotBlank String status,
        String assignedTo,
        String failureReason) {
}
