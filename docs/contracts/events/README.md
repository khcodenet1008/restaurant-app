# Event Contracts

Kafka is now used only for the simplified order and payment flow.

## Topics

| Topic | Producer | Consumers |
|---|---|---|
| `order.events` | `order-service` | optional audit or future consumers |
| `payment.events` | `payment-service` | `order-service` |
| `restaurant.dlq` | Kafka consumers | integration team |

## Event Envelope

```json
{
  "eventId": "uuid",
  "eventType": "PaymentAuthorized",
  "eventVersion": "v1",
  "sagaId": "uuid",
  "aggregateId": "order-1001",
  "occurredAt": "2026-07-02T10:00:00Z",
  "source": "payment-service",
  "traceId": "trace-id",
  "payload": {}
}
```

Use `orderId` as the Kafka message key for order-related events.

## Frozen Event List

- `OrderCreated.v1`
- `OrderCompleted.v1`
- `OrderCancelled.v1`
- `PaymentAuthorized.v1`
- `PaymentFailed.v1`

## Contract Rules

- events describe completed facts, not commands
- payloads include only needed fields
- failed consumer handling should move to `restaurant.dlq` after retries
