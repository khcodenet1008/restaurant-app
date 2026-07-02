package com.example.restaurant.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PaymentEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentEventPublisher.class);

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
            String body = objectMapper.writeValueAsString(message);

            CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(topicName, orderId, body)
                            .whenComplete((result, exception) -> {
                                if (exception != null) {
                                    LOGGER.warn("Kafka publish failed for payment event {} on order {}", eventType, orderId, exception);
                                }
                            });
                } catch (Exception exception) {
                    LOGGER.warn("Kafka publish skipped for payment event {} on order {}", eventType, orderId, exception);
                }
            });
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to publish payment event", exception);
        }
    }
}
