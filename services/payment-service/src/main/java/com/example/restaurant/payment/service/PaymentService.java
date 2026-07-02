package com.example.restaurant.payment.service;

import com.example.restaurant.payment.domain.PaymentAuthorization;
import com.example.restaurant.payment.repository.PaymentAuthorizationRepository;
import com.example.restaurant.payment.web.MockPaymentConfirmRequest;
import com.example.restaurant.payment.web.PaymentResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentAuthorizationRepository paymentAuthorizationRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(
            PaymentAuthorizationRepository paymentAuthorizationRepository,
            PaymentEventPublisher paymentEventPublisher) {
        this.paymentAuthorizationRepository = paymentAuthorizationRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Transactional
    public PaymentResponse confirm(MockPaymentConfirmRequest request) {
        PaymentAuthorization authorization = paymentAuthorizationRepository.findByOrderId(request.orderId())
                .orElseGet(() -> createManualAuthorization(request));

        authorization.setMockResult(request.approved() ? "APPROVED" : "FAILED");
        authorization.setStatus(request.approved() ? "AUTHORIZED" : "FAILED");
        authorization.setFailureReason(request.approved() ? null : request.failureReason());
        authorization.setAuthorizedAt(LocalDateTime.now());
        paymentAuthorizationRepository.save(authorization);

        Map<String, Object> payload = Map.of(
                "orderId", authorization.getOrderId(),
                "amount", authorization.getAmount(),
                "currency", authorization.getCurrency(),
                "reason", request.failureReason() == null ? "" : request.failureReason());
        paymentEventPublisher.publish(
                authorization.getOrderId(),
                request.approved() ? "PaymentAuthorized" : "PaymentFailed",
                authorization.getSagaId(),
                authorization.getTraceId(),
                payload);
        return PaymentResponse.from(authorization);
    }

    private PaymentAuthorization createManualAuthorization(MockPaymentConfirmRequest request) {
        PaymentAuthorization authorization = new PaymentAuthorization();
        authorization.setId("pay-" + UUID.randomUUID());
        authorization.setOrderId(request.orderId());
        authorization.setSagaId(request.sagaId());
        authorization.setCustomerId(request.customerId());
        authorization.setAmount(request.amount());
        authorization.setCurrency(request.currency());
        authorization.setPaymentMethod(request.paymentMethod());
        authorization.setStatus("PENDING");
        authorization.setTraceId(request.traceId());
        return paymentAuthorizationRepository.save(authorization);
    }
}
