package com.example.restaurant.notification.service;

import com.example.restaurant.notification.domain.NotificationAttempt;
import com.example.restaurant.notification.domain.ProcessedEvent;
import com.example.restaurant.notification.repository.NotificationAttemptRepository;
import com.example.restaurant.notification.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            "OrderCreated",
            "OrderCompleted",
            "OrderCancelled",
            "InventoryReservationFailed",
            "PaymentFailed",
            "KitchenTicketCompleted");

    private final NotificationAttemptRepository notificationAttemptRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    public NotificationService(
            NotificationAttemptRepository notificationAttemptRepository,
            ProcessedEventRepository processedEventRepository,
            NotificationEventPublisher notificationEventPublisher) {
        this.notificationAttemptRepository = notificationAttemptRepository;
        this.processedEventRepository = processedEventRepository;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @Transactional
    public void handleEvent(JsonNode event) {
        String eventId = event.path("eventId").asText();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        String eventType = event.path("eventType").asText();
        if (!SUPPORTED_EVENTS.contains(eventType)) {
            saveProcessedEvent(event);
            return;
        }

        JsonNode payload = event.path("payload");
        NotificationAttempt attempt = new NotificationAttempt();
        attempt.setId("notify-" + UUID.randomUUID());
        attempt.setOrderId(payload.path("orderId").asText(null));
        attempt.setCustomerId(payload.path("customerId").asText(null));
        attempt.setChannel("EMAIL");
        attempt.setNotificationType(eventType);
        attempt.setDestination("demo@restaurant.local");
        attempt.setStatus("SENT");
        attempt.setMessage("Simple classroom notification for " + eventType);
        attempt.setTraceId(event.path("traceId").asText(null));
        attempt.setRequestedEventId(eventId);
        attempt.setSentAt(LocalDateTime.now());
        notificationAttemptRepository.save(attempt);

        notificationEventPublisher.publish(
                attempt.getOrderId() == null ? "unknown-order" : attempt.getOrderId(),
                "NotificationSent",
                attempt.getTraceId(),
                Map.of(
                        "orderId", attempt.getOrderId() == null ? "" : attempt.getOrderId(),
                        "notificationType", eventType,
                        "channel", attempt.getChannel(),
                        "status", attempt.getStatus()));
        saveProcessedEvent(event);
    }

    private void saveProcessedEvent(JsonNode event) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.path("eventId").asText());
        processedEvent.setEventType(event.path("eventType").asText());
        processedEvent.setEventVersion(event.path("eventVersion").asText("v1"));
        processedEvent.setSourceService(event.path("source").asText());
        processedEvent.setAggregateId(event.path("payload").path("orderId").asText(null));
        processedEvent.setTraceId(event.path("traceId").asText(null));
        processedEventRepository.save(processedEvent);
    }
}
