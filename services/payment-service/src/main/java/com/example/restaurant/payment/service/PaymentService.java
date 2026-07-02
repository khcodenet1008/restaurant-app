package com.example.restaurant.payment.service;

import com.example.restaurant.payment.domain.PaymentAuthorization;
import com.example.restaurant.payment.domain.ProcessedEvent;
import com.example.restaurant.payment.repository.PaymentAuthorizationRepository;
import com.example.restaurant.payment.repository.ProcessedEventRepository;
import com.example.restaurant.payment.web.MockPaymentConfirmRequest;
import com.example.restaurant.payment.web.PaymentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentAuthorizationRepository paymentAuthorizationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(
            PaymentAuthorizationRepository paymentAuthorizationRepository,
            ProcessedEventRepository processedEventRepository,
            PaymentEventPublisher paymentEventPublisher) {
        this.paymentAuthorizationRepository = paymentAuthorizationRepository;
        this.processedEventRepository = processedEventRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Transactional
    public void createPendingAuthorization(JsonNode event) {
        String eventId = event.path("eventId").asText();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        JsonNode payload = event.path("payload");
        String orderId = payload.path("orderId").asText();
        paymentAuthorizationRepository.findByOrderId(orderId).orElseGet(() -> {
            PaymentAuthorization authorization = new PaymentAuthorization();
            authorization.setId("pay-" + UUID.randomUUID());
            authorization.setOrderId(orderId);
            authorization.setSagaId(event.path("sagaId").asText());
            authorization.setCustomerId(payload.path("customerId").asText("guest"));
            authorization.setAmount(payload.path("amount").decimalValue());
            authorization.setCurrency(payload.path("currency").asText("USD"));
            authorization.setPaymentMethod(payload.path("paymentMethod").asText("CASH"));
            authorization.setStatus("PENDING");
            authorization.setTraceId(event.path("traceId").asText(null));
            return paymentAuthorizationRepository.save(authorization);
        });

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setEventType(event.path("eventType").asText());
        processedEvent.setEventVersion(event.path("eventVersion").asText("v1"));
        processedEvent.setSourceService(event.path("source").asText("inventory-service"));
        processedEvent.setAggregateId(orderId);
        processedEvent.setTraceId(event.path("traceId").asText(null));
        processedEventRepository.save(processedEvent);
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
