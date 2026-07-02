package com.example.restaurant.order.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotBlank String paymentMethod,
        @NotBlank String currency,
        @Valid @NotEmpty List<CreateOrderItemRequest> items) {
}
