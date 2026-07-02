package com.example.restaurant.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public NotificationEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${NOTIFICATION_EVENTS_TOPIC:notification.events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    public void publish(String orderId, String eventType, String traceId, Map<String, Object> payload) {
        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("eventId", UUID.randomUUID().toString());
            message.put("eventType", eventType);
            message.put("eventVersion", "v1");
            message.put("sagaId", payload.getOrDefault("sagaId", ""));
            message.put("aggregateId", orderId);
            message.put("occurredAt", Instant.now().toString());
            message.put("source", "notification-service");
            message.put("traceId", traceId);
            message.put("payload", payload);
            kafkaTemplate.send(topicName, orderId, objectMapper.writeValueAsString(message));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to publish notification event", exception);
        }
    }
}
