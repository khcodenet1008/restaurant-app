package com.example.restaurant.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaEventListener {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public OrderSagaEventListener(ObjectMapper objectMapper, OrderService orderService) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${INVENTORY_EVENTS_TOPIC:inventory.events}", groupId = "${ORDER_CONSUMER_GROUP:order-service}")
    public void onInventoryEvent(String message) throws Exception {
        handle(message);
    }

    @KafkaListener(topics = "${PAYMENT_EVENTS_TOPIC:payment.events}", groupId = "${ORDER_CONSUMER_GROUP:order-service}")
    public void onPaymentEvent(String message) throws Exception {
        handle(message);
    }

    @KafkaListener(topics = "${KITCHEN_EVENTS_TOPIC:kitchen.events}", groupId = "${ORDER_CONSUMER_GROUP:order-service}")
    public void onKitchenEvent(String message) throws Exception {
        handle(message);
    }

    private void handle(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        String orderId = event.path("payload").path("orderId").asText(null);
        if (orderId != null && !orderId.isBlank()) {
            orderService.applyEvent(orderId, event);
        }
    }
}
