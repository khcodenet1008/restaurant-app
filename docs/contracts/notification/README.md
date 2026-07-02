# Notification Contracts

This page defines notification event consumption, simulated delivery, and audit record responsibilities.

## Kafka Ownership

Owner: Member 3

Published topic: `notification.events`

Published events:

- `NotificationSent.v1`
- `NotificationFailed.v1`

Consumed events:

- `OrderCreated.v1`
- `OrderCompleted.v1`
- `OrderCancelled.v1`
- `InventoryReservationFailed.v1`
- `PaymentFailed.v1`
- `KitchenTicketCompleted.v1`

The `notification-service` records simulated notification attempts and audit records. It does not send real email, SMS, or push messages in the first classroom version.

## CI/CD Responsibility

Member 3 owns final CI/CD assembly and keeps notification tests passing in GitHub Actions.
