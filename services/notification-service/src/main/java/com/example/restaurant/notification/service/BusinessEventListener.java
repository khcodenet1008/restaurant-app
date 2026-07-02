package com.example.restaurant.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BusinessEventListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public BusinessEventListener(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${ORDER_EVENTS_TOPIC:order.events}", groupId = "${NOTIFICATION_CONSUMER_GROUP:notification-service}")
    public void onOrderEvent(String message) throws Exception {
        handle(message);
    }

    @KafkaListener(topics = "${INVENTORY_EVENTS_TOPIC:inventory.events}", groupId = "${NOTIFICATION_CONSUMER_GROUP:notification-service}")
    public void onInventoryEvent(String message) throws Exception {
        handle(message);
    }

    @KafkaListener(topics = "${PAYMENT_EVENTS_TOPIC:payment.events}", groupId = "${NOTIFICATION_CONSUMER_GROUP:notification-service}")
    public void onPaymentEvent(String message) throws Exception {
        handle(message);
    }

    @KafkaListener(topics = "${KITCHEN_EVENTS_TOPIC:kitchen.events}", groupId = "${NOTIFICATION_CONSUMER_GROUP:notification-service}")
    public void onKitchenEvent(String message) throws Exception {
        handle(message);
    }

    private void handle(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        notificationService.handleEvent(event);
    }
}
