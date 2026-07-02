# Restaurant App Documentation

This folder is the shared documentation area for the restaurant microservices assignment.

## Chosen Integration Path

For the easiest course-aligned implementation, the project uses:

- Kafka for asynchronous service events and the restaurant order Saga.
- GitHub Actions for CI.
- Docker Hub for the classroom image registry.
- The existing `restaurant-gitops/` repository structure for CD through Argo CD.
- Kustomize overlays for `dev` and `demo` environments.

The full workflow is documented in [architecture/kafka-ci-cd-workflow.md](architecture/kafka-ci-cd-workflow.md).

## Documentation Map

| Area | File | Purpose |
|---|---|---|
| Architecture | [architecture/service-ownership.md](architecture/service-ownership.md) | Service ownership and shared integration responsibilities |
| Architecture | [architecture/kafka-ci-cd-workflow.md](architecture/kafka-ci-cd-workflow.md) | Kafka event flow and easiest CI/CD workflow |
| Standards | [standards/shared-standards.md](standards/shared-standards.md) | Shared coding, event, and delivery rules |
| Standards | [standards/config-parameters.md](standards/config-parameters.md) | Runtime parameters, topics, and secrets |
| Contracts | [contracts/events/README.md](contracts/events/README.md) | Shared Kafka event contracts |
| Contracts | `contracts/<service>/README.md` | Service-owned API and event responsibilities |
| Database | [database/README.md](database/README.md) | Schema ownership, Outbox, and idempotent consumer tables |

## Course Anchors

- Chapter 7 DevOps Concept and Practice, merged pages 394-420: CI, CD, GitOps, GitHub Actions, Docker image tags, and Argo CD.
- Chapter 14 Saga and Transaction, merged pages 789-839: Kafka event chain, Saga flow, compensation, idempotency, and DLQ.
- Chapter 15 Microservice Implementation, merged pages 840-905: service implementation, event/message handling, Outbox, and testing.
