# Order Contracts

This page defines order API ownership, order state transitions, and order event responsibilities.

## Kafka Ownership

Owner: Member 3

Published topic: `order.events`

Published events:

- `OrderCreated.v1`
- `OrderCompleted.v1`
- `OrderCancelled.v1`

Consumed events:

- `PaymentAuthorized.v1`
- `PaymentFailed.v1`

The `order-service` owns the order state and final order status. It does not access another service database directly.

## CI/CD Responsibility

Member 3 keeps order tests passing in GitHub Actions and provides the Dockerfile, ConfigMap, Deployment, Service, and GitOps image tag inputs for `order-service`.
