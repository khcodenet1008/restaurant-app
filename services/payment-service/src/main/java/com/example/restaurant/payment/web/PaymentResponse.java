package com.example.restaurant.payment.web;

import com.example.restaurant.payment.domain.PaymentAuthorization;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String id,
        String orderId,
        String sagaId,
        String customerId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String status,
        String mockResult,
        String failureReason,
        LocalDateTime authorizedAt) {

    public static PaymentResponse from(PaymentAuthorization authorization) {
        return new PaymentResponse(
                authorization.getId(),
                authorization.getOrderId(),
                authorization.getSagaId(),
                authorization.getCustomerId(),
                authorization.getAmount(),
                authorization.getCurrency(),
                authorization.getPaymentMethod(),
                authorization.getStatus(),
                authorization.getMockResult(),
                authorization.getFailureReason(),
                authorization.getAuthorizedAt());
    }
}
