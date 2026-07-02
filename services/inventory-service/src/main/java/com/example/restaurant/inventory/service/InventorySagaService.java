package com.example.restaurant.inventory.service;

import com.example.restaurant.inventory.domain.InventoryItem;
import com.example.restaurant.inventory.domain.ProcessedEvent;
import com.example.restaurant.inventory.domain.StockReservation;
import com.example.restaurant.inventory.domain.StockReservationLine;
import com.example.restaurant.inventory.repository.InventoryItemRepository;
import com.example.restaurant.inventory.repository.ProcessedEventRepository;
import com.example.restaurant.inventory.repository.StockReservationLineRepository;
import com.example.restaurant.inventory.repository.StockReservationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventorySagaService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;
    private final StockReservationLineRepository stockReservationLineRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryEventPublisher inventoryEventPublisher;

    public InventorySagaService(
            InventoryItemRepository inventoryItemRepository,
            StockReservationRepository stockReservationRepository,
            StockReservationLineRepository stockReservationLineRepository,
            ProcessedEventRepository processedEventRepository,
            InventoryEventPublisher inventoryEventPublisher) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockReservationRepository = stockReservationRepository;
        this.stockReservationLineRepository = stockReservationLineRepository;
        this.processedEventRepository = processedEventRepository;
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    @Transactional
    public void handleOrderCreated(JsonNode event) {
        String eventId = event.path("eventId").asText();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        JsonNode payload = event.path("payload");
        String orderId = payload.path("orderId").asText();
        String sagaId = event.path("sagaId").asText();
        String traceId = event.path("traceId").asText(null);

        List<JsonNode> requestedItems = new ArrayList<>();
        List<String> menuItemIds = new ArrayList<>();
        for (JsonNode itemNode : payload.path("items")) {
            requestedItems.add(itemNode);
            menuItemIds.add(itemNode.path("menuItemId").asText());
        }
        List<InventoryItem> inventoryItems = inventoryItemRepository.findByMenuItemIdIn(menuItemIds);
        Map<String, InventoryItem> inventoryMap = new HashMap<>();
        for (InventoryItem inventoryItem : inventoryItems) {
            inventoryMap.put(inventoryItem.getMenuItemId(), inventoryItem);
        }

        String failureReason = null;
        for (JsonNode itemNode : requestedItems) {
            InventoryItem inventoryItem = inventoryMap.get(itemNode.path("menuItemId").asText());
            int requestedQuantity = itemNode.path("quantity").asInt();
            if (inventoryItem == null || inventoryItem.getAvailableQuantity() < requestedQuantity) {
                failureReason = "Not enough stock for " + itemNode.path("menuItemId").asText();
                break;
            }
        }

        StockReservation reservation = new StockReservation();
        reservation.setId("res-" + UUID.randomUUID());
        reservation.setOrderId(orderId);
        reservation.setSagaId(sagaId);
        reservation.setTraceId(traceId);

        if (failureReason == null) {
            reservation.setStatus("RESERVED");
            stockReservationRepository.save(reservation);
            for (JsonNode itemNode : requestedItems) {
                InventoryItem inventoryItem = inventoryMap.get(itemNode.path("menuItemId").asText());
                int requestedQuantity = itemNode.path("quantity").asInt();
                inventoryItem.setAvailableQuantity(inventoryItem.getAvailableQuantity() - requestedQuantity);
                inventoryItem.setReservedQuantity(inventoryItem.getReservedQuantity() + requestedQuantity);
                inventoryItemRepository.save(inventoryItem);

                StockReservationLine line = new StockReservationLine();
                line.setReservationId(reservation.getId());
                line.setInventoryItemId(inventoryItem.getId());
                line.setMenuItemId(inventoryItem.getMenuItemId());
                line.setRequestedQuantity(requestedQuantity);
                line.setReservedQuantity(requestedQuantity);
                stockReservationLineRepository.save(line);
            }

            inventoryEventPublisher.publish(orderId, "InventoryReserved", sagaId, traceId, Map.of(
                    "orderId", orderId,
                    "customerId", payload.path("customerId").asText(),
                    "paymentMethod", payload.path("paymentMethod").asText(),
                    "amount", payload.path("amount").decimalValue(),
                    "currency", payload.path("currency").asText("USD")));
        } else {
            reservation.setStatus("FAILED");
            reservation.setFailureReason(failureReason);
            stockReservationRepository.save(reservation);
            inventoryEventPublisher.publish(orderId, "InventoryReservationFailed", sagaId, traceId, Map.of(
                    "orderId", orderId,
                    "reason", failureReason));
        }

        saveProcessedEvent(eventId, event.path("eventType").asText(), event.path("source").asText(), orderId, traceId);
    }

    @Transactional
    public void releaseReservation(JsonNode event) {
        String eventId = event.path("eventId").asText();
        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        String orderId = event.path("payload").path("orderId").asText();
        StockReservation reservation = stockReservationRepository.findByOrderId(orderId).orElse(null);
        if (reservation != null && "RESERVED".equals(reservation.getStatus())) {
            List<StockReservationLine> lines = stockReservationLineRepository.findByReservationId(reservation.getId());
            for (StockReservationLine line : lines) {
                InventoryItem inventoryItem = inventoryItemRepository.findById(line.getInventoryItemId()).orElse(null);
                if (inventoryItem != null) {
                    inventoryItem.setAvailableQuantity(inventoryItem.getAvailableQuantity() + line.getReservedQuantity());
                    inventoryItem.setReservedQuantity(inventoryItem.getReservedQuantity() - line.getReservedQuantity());
                    inventoryItemRepository.save(inventoryItem);
                }
            }
            reservation.setStatus("RELEASED");
            stockReservationRepository.save(reservation);
            inventoryEventPublisher.publish(
                    orderId,
                    "InventoryReleased",
                    reservation.getSagaId(),
                    reservation.getTraceId(),
                    Map.of("orderId", orderId, "reason", event.path("eventType").asText()));
        }

        saveProcessedEvent(eventId, event.path("eventType").asText(), event.path("source").asText(), orderId, event.path("traceId").asText(null));
    }

    private void saveProcessedEvent(
            String eventId,
            String eventType,
            String source,
            String aggregateId,
            String traceId) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setEventType(eventType);
        processedEvent.setEventVersion("v1");
        processedEvent.setSourceService(source);
        processedEvent.setAggregateId(aggregateId);
        processedEvent.setTraceId(traceId);
        processedEventRepository.save(processedEvent);
    }
}
