# Event Contracts

Kafka is the chosen event backbone for this assignment. Use the direct Spring Kafka path from [../../architecture/kafka-ci-cd-workflow.md](../../architecture/kafka-ci-cd-workflow.md): `KafkaTemplate` for publishing, `@KafkaListener` for consuming, `outbox_event` for reliable publication, and `processed_event` for idempotent consumption.

## Topics

| Topic | Producer | Consumers |
|---|---|---|
| `order.events` | `order-service` | `inventory-service`, `notification-service` |
| `inventory.events` | `inventory-service` | `payment-service`, `order-service`, `notification-service` |
| `payment.events` | `payment-service` | `kitchen-service`, `inventory-service`, `order-service`, `notification-service` |
| `kitchen.events` | `kitchen-service` | `order-service`, `notification-service` |
| `notification.events` | `notification-service` | optional audit consumers |
| `restaurant.dlq` | all consumers | integration team |

## Event Envelope

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "eventVersion": "v1",
  "sagaId": "uuid",
  "aggregateId": "order-1001",
  "occurredAt": "2026-07-02T10:00:00Z",
  "source": "order-service",
  "traceId": "trace-id",
  "payload": {}
}
```

Use `orderId` as the Kafka message key for order Saga events.

## Frozen Event List

- `OrderCreated.v1`
- `InventoryReserved.v1`
- `InventoryReservationFailed.v1`
- `InventoryReleased.v1`
- `PaymentAuthorized.v1`
- `PaymentFailed.v1`
- `KitchenTicketCreated.v1`
- `KitchenTicketCompleted.v1`
- `OrderCompleted.v1`
- `OrderCancelled.v1`
- `NotificationSent.v1`
- `NotificationFailed.v1`

## Contract Rules

- Each event contract must document producer, topic, key, payload fields, success consumers, and failure behavior.
- Event payloads must include only fields needed by other services.
- Consumers must ignore duplicate `eventId` values already stored in `processed_event`.
- Failed events must retry before being sent to `restaurant.dlq`.
