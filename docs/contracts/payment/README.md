# Payment Contracts

This page defines mock payment API ownership and payment event responsibilities.

## Kafka Ownership

Owner: Member 3

Published topic: `payment.events`

Published events:

- `PaymentAuthorized.v1`
- `PaymentFailed.v1`

Consumed events:

- `InventoryReserved.v1`

The `payment-service` performs mock authorization only after inventory is reserved. For the classroom version, real payment provider integration stays out of scope.

## CI/CD Responsibility

Member 3 keeps payment tests passing in GitHub Actions and provides the Dockerfile, ConfigMap, Deployment, Service, and GitOps image tag inputs for `payment-service`.
