package com.example.restaurant.kitchen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final KitchenService kitchenService;

    public PaymentEventListener(ObjectMapper objectMapper, KitchenService kitchenService) {
        this.objectMapper = objectMapper;
        this.kitchenService = kitchenService;
    }

    @KafkaListener(topics = "${PAYMENT_EVENTS_TOPIC:payment.events}", groupId = "${KITCHEN_CONSUMER_GROUP:kitchen-service}")
    public void onPaymentEvent(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        if ("PaymentAuthorized".equals(event.path("eventType").asText())) {
            kitchenService.createTicketFromPayment(event);
        }
    }
}
