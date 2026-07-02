package com.example.restaurant.order.service;

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
public class OrderEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public OrderEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${ORDER_EVENTS_TOPIC:order.events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    public void publish(
            String orderId,
            String eventId,
            String eventType,
            String sagaId,
            String traceId,
            Map<String, Object> payload) {
        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("eventId", eventId);
            message.put("eventType", eventType);
            message.put("eventVersion", "v1");
            message.put("sagaId", sagaId);
            message.put("aggregateId", orderId);
            message.put("occurredAt", Instant.now().toString());
            message.put("source", "order-service");
            message.put("traceId", traceId);
            message.put("payload", payload);
            String body = objectMapper.writeValueAsString(message);

            CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(topicName, orderId, body)
                            .whenComplete((result, exception) -> {
                                if (exception != null) {
                                    LOGGER.warn("Kafka publish failed for order event {} on order {}", eventType, orderId, exception);
                                }
                            });
                } catch (Exception exception) {
                    LOGGER.warn("Kafka publish skipped for order event {} on order {}", eventType, orderId, exception);
                }
            });
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to publish order event", exception);
        }
    }
}
