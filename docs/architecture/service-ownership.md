# Service Ownership

This boilerplate follows an independence-first ownership model.

## Service Owners

- Member 1: `gateway-service`, `menu-service`
- Member 2: `order-service`
- Member 3: `payment-service`, `notification-service`
- Member 4: `inventory-service`, `kitchen-service`

## Ownership Boundaries

- One service has one owner.
- One database schema has one owner.
- One service-local manifest folder has one owner.
- One service contract folder has one owner.

## Shared Integration Rule

Shared components such as MySQL, Kafka, Keycloak, CI, GitOps overlays, and observability should only be assembled after service-local contracts are frozen.
