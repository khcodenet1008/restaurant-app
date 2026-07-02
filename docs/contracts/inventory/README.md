# Inventory Contracts

This page defines inventory reservation, release, and inventory event responsibilities.

## Kafka Ownership

Owner: Member 4

Published topic: `inventory.events`

Published events:

- `InventoryReserved.v1`
- `InventoryReservationFailed.v1`
- `InventoryReleased.v1`

Consumed events:

- `OrderCreated.v1`
- `PaymentFailed.v1`
- `OrderCancelled.v1`

The `inventory-service` reserves stock after an order is created and releases stock during compensation when payment fails or the order is cancelled.

## CI/CD Responsibility

Member 4 keeps inventory tests passing in GitHub Actions and provides the Dockerfile, ConfigMap, Deployment, Service, and GitOps image tag inputs for `inventory-service`.
