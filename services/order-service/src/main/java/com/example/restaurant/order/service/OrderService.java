package com.example.restaurant.order.service;

import com.example.restaurant.order.domain.CustomerOrder;
import com.example.restaurant.order.domain.OrderItem;
import com.example.restaurant.order.domain.SagaState;
import com.example.restaurant.order.repository.CustomerOrderRepository;
import com.example.restaurant.order.repository.OrderItemRepository;
import com.example.restaurant.order.repository.SagaStateRepository;
import com.example.restaurant.order.web.CancelOrderRequest;
import com.example.restaurant.order.web.CreateOrderRequest;
import com.example.restaurant.order.web.OrderItemResponse;
import com.example.restaurant.order.web.OrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SagaStateRepository sagaStateRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(
            CustomerOrderRepository customerOrderRepository,
            OrderItemRepository orderItemRepository,
            SagaStateRepository sagaStateRepository,
            OrderEventPublisher orderEventPublisher) {
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.sagaStateRepository = sagaStateRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String traceId) {
        String orderId = "order-" + UUID.randomUUID();
        String sagaId = UUID.randomUUID().toString();
        BigDecimal subtotal = request.items()
                .stream()
                .map(item -> item.unitPriceAmount().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CustomerOrder order = new CustomerOrder();
        order.setId(orderId);
        order.setCustomerId(request.customerId());
        order.setSagaId(sagaId);
        order.setStatus("PENDING");
        order.setPaymentMethod(request.paymentMethod());
        order.setSubtotalAmount(subtotal);
        order.setCurrency(request.currency());
        order.setTraceId(traceId);
        customerOrderRepository.save(order);

        List<OrderItem> items = request.items().stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setMenuItemId(item.menuItemId());
            orderItem.setMenuItemName(item.menuItemName());
            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPriceAmount(item.unitPriceAmount());
            orderItem.setLineTotalAmount(item.unitPriceAmount().multiply(BigDecimal.valueOf(item.quantity())));
            orderItem.setCurrency(request.currency());
            return orderItem;
        }).toList();
        orderItemRepository.saveAll(items);

        SagaState sagaState = new SagaState();
        sagaState.setSagaId(sagaId);
        sagaState.setOrderId(orderId);
        sagaState.setCurrentStep("ORDER_CREATED");
        sagaState.setStatus("IN_PROGRESS");
        sagaStateRepository.save(sagaState);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId);
        payload.put("customerId", request.customerId());
        payload.put("paymentMethod", request.paymentMethod());
        payload.put("amount", subtotal);
        payload.put("currency", request.currency());
        payload.put("items", items.stream().map(item -> Map.of(
                "menuItemId", item.getMenuItemId(),
                "menuItemName", item.getMenuItemName(),
                "quantity", item.getQuantity(),
                "unitPriceAmount", item.getUnitPriceAmount())).toList());
        orderEventPublisher.publish(orderId, UUID.randomUUID().toString(), "OrderCreated", sagaId, traceId, payload);

        return toResponse(order, items);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return toResponse(order, orderItemRepository.findByOrderId(orderId));
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId, CancelOrderRequest request) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if ("COMPLETED".equals(order.getStatus())) {
            throw new IllegalStateException("Completed order cannot be cancelled");
        }

        order.setStatus("CANCELLED");
        order.setCancelReason(request.reason());
        customerOrderRepository.save(order);

        SagaState sagaState = sagaStateRepository.findByOrderId(orderId).orElse(null);
        if (sagaState != null) {
            sagaState.setCurrentStep("ORDER_CANCELLED");
            sagaState.setStatus("FAILED");
            sagaState.setFailureReason(request.reason());
            sagaStateRepository.save(sagaState);
        }

        Map<String, Object> payload = Map.of(
                "orderId", order.getId(),
                "reason", request.reason() == null ? "Cancelled by API request" : request.reason());
        orderEventPublisher.publish(
                order.getId(),
                UUID.randomUUID().toString(),
                "OrderCancelled",
                order.getSagaId(),
                order.getTraceId(),
                payload);
        return toResponse(order, orderItemRepository.findByOrderId(orderId));
    }

    @Transactional
    public void applyEvent(String orderId, JsonNode event) {
        CustomerOrder order = customerOrderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }

        String eventType = event.path("eventType").asText();
        SagaState sagaState = sagaStateRepository.findByOrderId(orderId).orElse(null);
        String eventId = event.path("eventId").asText(null);

        switch (eventType) {
            case "PaymentAuthorized" -> {
                updateOrder(order, sagaState, "COMPLETED", "ORDER_COMPLETED", "COMPLETED", eventId, null);
                Map<String, Object> payload = Map.of("orderId", order.getId(), "status", "COMPLETED");
                orderEventPublisher.publish(
                        order.getId(),
                        UUID.randomUUID().toString(),
                        "OrderCompleted",
                        order.getSagaId(),
                        order.getTraceId(),
                        payload);
            }
            case "PaymentFailed" -> updateOrder(order, sagaState, "CANCELLED", "PAYMENT_FAILED", "FAILED", eventId, readReason(event));
            default -> {
                return;
            }
        }
    }

    private void updateOrder(
            CustomerOrder order,
            SagaState sagaState,
            String orderStatus,
            String step,
            String sagaStatus,
            String lastEventId,
            String failureReason) {
        order.setStatus(orderStatus);
        if (failureReason != null && !failureReason.isBlank()) {
            order.setCancelReason(failureReason);
        }
        customerOrderRepository.save(order);

        if (sagaState != null) {
            sagaState.setCurrentStep(step);
            sagaState.setStatus(sagaStatus);
            sagaState.setLastEventId(lastEventId);
            sagaState.setFailureReason(failureReason);
            sagaStateRepository.save(sagaState);
        }
    }

    private String readReason(JsonNode event) {
        JsonNode payload = event.path("payload");
        if (payload.hasNonNull("reason")) {
            return payload.path("reason").asText();
        }
        if (payload.hasNonNull("failureReason")) {
            return payload.path("failureReason").asText();
        }
        return "Saga step failed";
    }

    private OrderResponse toResponse(CustomerOrder order, List<OrderItem> items) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getSagaId(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getSubtotalAmount(),
                order.getCurrency(),
                order.getCancelReason(),
                order.getCreatedAt(),
                items.stream()
                        .map(item -> new OrderItemResponse(
                                item.getMenuItemId(),
                                item.getMenuItemName(),
                                item.getQuantity(),
                                item.getUnitPriceAmount(),
                                item.getLineTotalAmount(),
                                item.getCurrency()))
                        .toList());
    }
}
