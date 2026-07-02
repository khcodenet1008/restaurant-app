# Service Ownership

This boilerplate follows an independence-first ownership model.

## Service Owners

- Member 1: `gateway-service`, `menu-service`
- Member 2: `order-service`
- Member 3: `payment-service`, `notification-service`
- Member 4: `inventory-service`, `kitchen-service`

## Chosen Shared Workflow

The easiest shared integration path is documented in [kafka-ci-cd-workflow.md](kafka-ci-cd-workflow.md).

- Kafka carries asynchronous Saga events between services.
- GitHub Actions runs CI for all services.
- Docker Hub stores deployable service images.
- `restaurant-gitops/` stores the desired Kubernetes state.
- Argo CD watches the GitOps repo and performs CD into Kubernetes.

## Ownership Boundaries

- One service has one owner.
- One database schema has one owner.
- One service-local manifest folder has one owner.
- One service contract folder has one owner.

## Shared Integration Rule

Shared components such as MySQL, Kafka, Keycloak, CI, GitOps overlays, and observability should only be assembled after service-local contracts are frozen.

Member 3 assembles the CI/CD workflow during integration, but every service owner must keep their service tests, Dockerfile, ConfigMap, Deployment, Service, and event contracts ready for the shared pipeline.
