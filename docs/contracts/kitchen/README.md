# Kitchen Contracts

This page defines kitchen ticket API ownership and kitchen event responsibilities.

## Kafka Ownership

Owner: Member 4

Published topic: `kitchen.events`

Published events:

- `KitchenTicketCreated.v1`
- `KitchenTicketCompleted.v1`

Consumed events:

- `PaymentAuthorized.v1`

The `kitchen-service` creates kitchen tickets only after payment is authorized. Public kitchen staff APIs still go through `gateway-service`.

## CI/CD Responsibility

Member 4 keeps kitchen tests passing in GitHub Actions and provides the Dockerfile, ConfigMap, Deployment, Service, and GitOps image tag inputs for `kitchen-service`.
