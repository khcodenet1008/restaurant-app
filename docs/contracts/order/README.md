# Order Contracts

This page defines order API ownership, order state transitions, and order event responsibilities.

## Kafka Ownership

Owner: Member 2

Published topic: `order.events`

Published events:

- `OrderCreated.v1`
- `OrderCompleted.v1`
- `OrderCancelled.v1`

Consumed events:

- `InventoryReserved.v1`
- `InventoryReservationFailed.v1`
- `PaymentAuthorized.v1`
- `PaymentFailed.v1`
- `KitchenTicketCompleted.v1`

The `order-service` owns the Saga state and final order status. It does not call inventory, payment, kitchen, or notification databases directly.

## CI/CD Responsibility

Member 2 keeps order tests passing in GitHub Actions and provides the Dockerfile, ConfigMap, Deployment, Service, and GitOps image tag inputs for `order-service`.
