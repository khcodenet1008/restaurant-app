package com.example.restaurant.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public PaymentEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${PAYMENT_EVENTS_TOPIC:payment.events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    public void publish(
            String orderId,
            String eventType,
            String sagaId,
            String traceId,
            Map<String, Object> payload) {
        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("eventId", java.util.UUID.randomUUID().toString());
            message.put("eventType", eventType);
            message.put("eventVersion", "v1");
            message.put("sagaId", sagaId);
            message.put("aggregateId", orderId);
            message.put("occurredAt", Instant.now().toString());
            message.put("source", "payment-service");
            message.put("traceId", traceId);
            message.put("payload", payload);
            kafkaTemplate.send(topicName, orderId, objectMapper.writeValueAsString(message));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to publish payment event", exception);
        }
    }
}
