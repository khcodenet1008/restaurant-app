package com.example.restaurant.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public InventoryEventListener(ObjectMapper objectMapper, PaymentService paymentService) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${INVENTORY_EVENTS_TOPIC:inventory.events}", groupId = "${PAYMENT_CONSUMER_GROUP:payment-service}")
    public void onInventoryEvent(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        if ("InventoryReserved".equals(event.path("eventType").asText())) {
            paymentService.createPendingAuthorization(event);
        }
    }
}
