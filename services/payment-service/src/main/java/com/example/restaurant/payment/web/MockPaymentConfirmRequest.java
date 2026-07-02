package com.example.restaurant.payment.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MockPaymentConfirmRequest(
        @NotBlank String orderId,
        @NotBlank String sagaId,
        @NotBlank String customerId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String paymentMethod,
        @NotBlank String traceId,
        boolean approved,
        String failureReason) {
}
