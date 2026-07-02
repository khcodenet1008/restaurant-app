# Restaurant App

Simple classroom baseline for the CBLM restaurant microservices assignment.

## Repositories

- `restaurant-app/`: application code, service-local config, service-local manifests, contracts, and docs
- `restaurant-gitops/`: GitOps desired state, overlays, and release-time deployment assembly

## Services

- `gateway-service`
- `menu-service`
- `order-service`
- `payment-service`
- `notification-service`
- `inventory-service`
- `kitchen-service`

## Structure

- `services/`: one folder per service owner
- `docs/assignment/`: split-work guide, full-flow guide, and short summary copied into the repo
- `docs/contracts/`: API, route, and event contracts
- `docs/standards/`: frozen team conventions and config reference
- `docs/database/`: SQL template guide and database ownership notes
- `platform/`: service-local Kubernetes manifests and shared platform base
- `.github/workflows/`: CI workflow boilerplate

## Current Baseline

- `gateway-service` forwards public API routes and adds a correlation ID header
- `menu-service` provides menu item read APIs
- `order-service` creates, reads, and cancels orders
- `payment-service` provides a mock confirm API
- `inventory-service`, `payment-service`, `kitchen-service`, `order-service`, and `notification-service` are connected with a simple Kafka choreography flow
- service-local schemas and Flyway migrations are included for all business services
- Kubernetes manifests and GitOps repo structure are prepared for a Docker Hub and Argo CD classroom workflow

## Ownership Model

- Member 1: `gateway-service`, `menu-service`
- Member 2: `order-service`
- Member 3: `payment-service`, `notification-service`
- Member 4: `inventory-service`, `kitchen-service`

## Next Steps

1. Add Maven Wrapper or install Maven locally to run the build and tests.
2. Replace direct Kafka publish with the simple outbox relay described in `docs/architecture/kafka-ci-cd-workflow.md`.
3. Add shared error handling and integration tests for the public APIs and Kafka listeners.
4. Finish GitHub Actions image build plus GitOps SHA tag update.
5. Verify the full demo flow in Kubernetes through `restaurant-gitops/`.
