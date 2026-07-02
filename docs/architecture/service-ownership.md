# Service Ownership

This repository now uses a simplified 4-service model.

## Service Owners

- Member 1: `gateway-service`
- Member 2: `menu-service`
- Member 3: `order-service`
- Member 4: `payment-service`

## Shared Workflow

The main delivery flow is documented in [kafka-ci-cd-workflow.md](kafka-ci-cd-workflow.md).

- `gateway-service` handles HTTP entry and route forwarding
- `menu-service` provides menu read APIs
- `order-service` creates, reads, and cancels orders
- `payment-service` confirms mock payments and publishes payment result events

## Ownership Boundaries

- one service has one owner
- one schema has one owner
- one service-local manifest folder has one owner
- one contract folder has one owner

## Shared Integration Rule

Shared components such as MySQL, Kafka, Keycloak, CI, GitOps overlays, and Istio should be integrated after each service contract is stable.
