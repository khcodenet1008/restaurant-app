package com.example.restaurant.kitchen.service;

import com.example.restaurant.kitchen.domain.KitchenTicket;
import com.example.restaurant.kitchen.domain.KitchenTicketItem;
import com.example.restaurant.kitchen.domain.ProcessedEvent;
import com.example.restaurant.kitchen.repository.KitchenTicketItemRepository;
import com.example.restaurant.kitchen.repository.KitchenTicketRepository;
import com.example.restaurant.kitchen.repository.ProcessedEventRepository;
import com.example.restaurant.kitchen.web.KitchenTicketItemResponse;
import com.example.restaurant.kitchen.web.KitchenTicketResponse;
import com.example.restaurant.kitchen.web.UpdateKitchenTicketStatusRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KitchenService {

    private final KitchenTicketRepository kitchenTicketRepository;
    private final KitchenTicketItemRepository kitchenTicketItemRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final KitchenEventPublisher kitchenEventPublisher;
    private final String defaultQueueName;

    public KitchenService(
            KitchenTicketRepository kitchenTicketRepository,
            KitchenTicketItemRepository kitchenTicketItemRepository,
            ProcessedEventRepository processedEventRepository,
            KitchenEventPublisher kitchenEventPublisher,
            @Value("${KITCHEN_DEFAULT_QUEUE:main-line}") String defaultQueueName) {
        this.kitchenTicketRepository = kitchenTicketRepository;
        this.kitchenTicketItemRepository = kitchenTicketItemRepository;
        this.processedEventRepository = processedEventRepository;
        this.kitchenEventPublisher = kitchenEventPublisher;
        this.defaultQueueName = defaultQueueName;
    }

    @Transactional
    public void createTicketFromPayment(JsonNode event) {
        String eventId = event.path("eventId").asText();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        JsonNode payload = event.path("payload");
        String orderId = payload.path("orderId").asText();
        if (kitchenTicketRepository.findByOrderId(orderId).isPresent()) {
            saveProcessedEvent(event);
            return;
        }

        KitchenTicket ticket = new KitchenTicket();
        ticket.setId("ticket-" + UUID.randomUUID());
        ticket.setOrderId(orderId);
        ticket.setSagaId(event.path("sagaId").asText());
        ticket.setQueueName(defaultQueueName);
        ticket.setStatus("PENDING");
        ticket.setPriority(0);
        ticket.setTraceId(event.path("traceId").asText(null));
        kitchenTicketRepository.save(ticket);

        kitchenEventPublisher.publish(
                orderId,
                "KitchenTicketCreated",
                ticket.getSagaId(),
                ticket.getTraceId(),
                Map.of("orderId", orderId, "ticketId", ticket.getId(), "status", ticket.getStatus()));
        saveProcessedEvent(event);
    }

    @Transactional(readOnly = true)
    public List<KitchenTicketResponse> getTickets(String status) {
        List<KitchenTicket> tickets = (status == null || status.isBlank())
                ? kitchenTicketRepository.findAllByOrderByCreatedAtAsc()
                : kitchenTicketRepository.findByStatusOrderByCreatedAtAsc(status);
        return tickets.stream().map(this::toResponse).toList();
    }

    @Transactional
    public KitchenTicketResponse updateStatus(String ticketId, UpdateKitchenTicketStatusRequest request) {
        KitchenTicket ticket = kitchenTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Kitchen ticket not found: " + ticketId));
        ticket.setStatus(request.status());
        ticket.setAssignedTo(request.assignedTo());
        ticket.setFailureReason(request.failureReason());
        if ("IN_PROGRESS".equals(request.status()) && ticket.getAcceptedAt() == null) {
            ticket.setAcceptedAt(LocalDateTime.now());
        }
        if ("COMPLETED".equals(request.status())) {
            ticket.setCompletedAt(LocalDateTime.now());
            kitchenEventPublisher.publish(
                    ticket.getOrderId(),
                    "KitchenTicketCompleted",
                    ticket.getSagaId(),
                    ticket.getTraceId(),
                    Map.of("orderId", ticket.getOrderId(), "ticketId", ticket.getId(), "status", "COMPLETED"));
        }
        kitchenTicketRepository.save(ticket);
        return toResponse(ticket);
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

    private KitchenTicketResponse toResponse(KitchenTicket ticket) {
        List<KitchenTicketItemResponse> items = kitchenTicketItemRepository.findByTicketId(ticket.getId())
                .stream()
                .map(item -> new KitchenTicketItemResponse(
                        item.getMenuItemId(),
                        item.getMenuItemName(),
                        item.getQuantity(),
                        item.getSpecialInstruction()))
                .toList();
        return new KitchenTicketResponse(
                ticket.getId(),
                ticket.getOrderId(),
                ticket.getSagaId(),
                ticket.getQueueName(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getAssignedTo(),
                ticket.getFailureReason(),
                ticket.getCreatedAt(),
                ticket.getAcceptedAt(),
                ticket.getCompletedAt(),
                items);
    }
}
