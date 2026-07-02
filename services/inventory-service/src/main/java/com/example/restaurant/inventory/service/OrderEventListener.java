package com.example.restaurant.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final InventorySagaService inventorySagaService;

    public OrderEventListener(ObjectMapper objectMapper, InventorySagaService inventorySagaService) {
        this.objectMapper = objectMapper;
        this.inventorySagaService = inventorySagaService;
    }

    @KafkaListener(topics = "${ORDER_EVENTS_TOPIC:order.events}", groupId = "${INVENTORY_CONSUMER_GROUP:inventory-service}")
    public void onOrderEvent(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        String eventType = event.path("eventType").asText();
        if ("OrderCreated".equals(eventType)) {
            inventorySagaService.handleOrderCreated(event);
        } else if ("OrderCancelled".equals(eventType)) {
            inventorySagaService.releaseReservation(event);
        }
    }

    @KafkaListener(topics = "${PAYMENT_EVENTS_TOPIC:payment.events}", groupId = "${INVENTORY_CONSUMER_GROUP:inventory-service}")
    public void onPaymentEvent(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        if ("PaymentFailed".equals(event.path("eventType").asText())) {
            inventorySagaService.releaseReservation(event);
        }
    }
}
