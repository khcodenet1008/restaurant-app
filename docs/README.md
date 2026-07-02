# Restaurant App Documentation

This folder documents the simplified restaurant assignment with 4 services only:

- `gateway-service`
- `menu-service`
- `order-service`
- `payment-service`

Chosen delivery path:

- GitHub Actions for CI
- Docker Hub for images
- GitOps through `restaurant-gitops/`
- Argo CD style deployment flow
- Kafka for simple order and payment events
- Istio only for the focused 4-service mesh demo

## Documentation Map

| Area | File | Purpose |
|---|---|---|
| Architecture | [architecture/service-ownership.md](architecture/service-ownership.md) | 4-service ownership and integration responsibilities |
| Architecture | [architecture/kafka-ci-cd-workflow.md](architecture/kafka-ci-cd-workflow.md) | Runtime flow, Kafka topics, CI, CD, and Istio relationship |
| Architecture | [architecture/system-communication-graph.md](architecture/system-communication-graph.md) | Whole-system communication graph with ports and reasons |
| Standards | [standards/shared-standards.md](standards/shared-standards.md) | Shared coding, event, and delivery rules |
| Standards | [standards/config-parameters.md](standards/config-parameters.md) | Runtime parameters, topics, and secrets |
| Contracts | [contracts/events/README.md](contracts/events/README.md) | Shared event contracts for the 4-service system |
| Contracts | `contracts/<service>/README.md` | Service-owned API and event responsibilities |
| Database | [database/README.md](database/README.md) | Schema ownership and migration notes |

## Course Anchors

- Chapter 7 DevOps Concept and Practice, merged pages 394-420
- Chapter 9 API Gateway, merged pages 469-538
- Chapter 12 Service Mesh, merged pages 688-748
- Chapter 14 Saga and Transaction, merged pages 789-839
