# Restaurant App Assignment Split for 4 Members

This version is optimized for independent service ownership. The goal is that each member can work on their own service lane with minimal overlap, which matches the course direction on independent domains, separate ownership, and DB-per-service design.

Course grounding:

- Chapter 9 API gateway, merged pages 469-538: API Gateway is the single entry point, handles routing, authentication, token relay, header transformation, CORS, timeout, retry, circuit breaker, and traffic control.
- Chapter 11 service separation and data design, merged pages 665-675: one service should have one clear role, each service owns its own data, and services should not access other service databases directly.
- Chapter 10 microservice design detail, merged pages 596-607: service boundaries should be independent, and event issuers and consumers should stay loosely coupled.
- Chapter 5 MSA concepts and design principles, merged pages 207-209: each microservice should be developed and deployed independently by individual teams.

## Independence-First Working Model

Use these rules for the whole assignment:

- One service or service lane has one owner.
- One schema has one owner.
- One service folder has one owner.
- One service Dockerfile and one service manifest folder have one owner.
- The API Gateway is a standalone module, not embedded inside a business service.
- No member edits another member's business logic files.
- No service reads or writes another service database.
- Service-to-service cooperation happens through versioned APIs, events, or documented route contracts.
- Shared folders are frozen early and changed only by agreement.

## Frozen Shared Standards Before Coding

Finish these together in Part 0 and Part 1, then freeze them:

- package naming: `com.example.restaurant.<service>`
- error response format: `code`, `message`, `traceId`, `timestamp`
- correlation header: `X-Correlation-Id`
- gateway implementation choice: default to standalone `gateway-service` using Spring Cloud Gateway
- gateway route contract template: path, method, target service, auth role, timeout, retry, token relay, correlation ID behavior
- event envelope fields: `eventId`, `eventType`, `eventVersion`, `sagaId`, `aggregateId`, `occurredAt`, `source`, `traceId`, `payload`
- Kafka topics: `order.events`, `inventory.events`, `payment.events`, `kitchen.events`, `notification.events`, `restaurant.dlq`
- CI/CD path: GitHub Actions, Docker Hub SHA image tags, `restaurant-gitops/`, and Argo CD sync
- repository structure
- branch naming convention
- PR template and review rule
- folder naming for manifests and docs

After these are frozen, each member should work only inside their owned folders.

## Team Structure

Use 4 independent service lanes. The gateway is a required standalone module owned by Member 1, and every other member provides route contracts for the endpoints they own.

- Member 1: standalone `gateway-service` plus `menu-service`
- Member 2: `order-service`
- Member 3: `payment-service` and `notification-service`
- Member 4: `inventory-service` and `kitchen-service`

Why this split works:

- `gateway-service` is a standalone edge module that exposes all public APIs without owning business logic.
- `menu-service` is read-heavy and pairs naturally with the gateway owner for early smoke testing.
- `order-service` is the core workflow and Saga coordinator, so it deserves a dedicated owner.
- `payment-service` and `notification-service` are side-effect services that fit one independent lane.
- `inventory-service` and `kitchen-service` are fulfillment services and can be built together without touching order or payment code.

## Chosen Kafka and CI/CD Workflow

Use the easiest course-aligned workflow for this assignment:

- Kafka is the shared event broker for the order Saga.
- Use a single-node demo Kafka deployment for classroom and local Kubernetes.
- Use Spring `KafkaTemplate` for publishing and `@KafkaListener` for consuming.
- Use `outbox_event` for reliable event publication and `processed_event` for idempotent consumers.
- Use choreography first, not a separate orchestrator service.
- Use GitHub Actions for CI.
- Use Docker Hub for the classroom image registry.
- Use immutable commit SHA image tags for GitOps deployment.
- Use `restaurant-gitops/` as the desired-state repository.
- Use Argo CD to sync GitOps changes into Kubernetes.

Kafka event flow:

```text
POST /api/orders
  -> OrderCreated.v1
  -> InventoryReserved.v1 or InventoryReservationFailed.v1
  -> PaymentAuthorized.v1 or PaymentFailed.v1
  -> KitchenTicketCreated.v1
  -> KitchenTicketCompleted.v1
  -> OrderCompleted.v1 or OrderCancelled.v1
  -> NotificationSent.v1 or NotificationFailed.v1
```

Failure and compensation:

- `InventoryReservationFailed.v1` cancels the order and triggers notification.
- `PaymentFailed.v1` releases inventory, cancels the order, and triggers notification.
- Events that still fail after retries go to `restaurant.dlq`.

## Required API Gateway Module

The assignment must include a standalone `gateway-service` module.

Gateway implementation choice:

- default: Spring Cloud Gateway
- Kubernetes edge option: NGINX Ingress or Istio Gateway routes traffic to `gateway-service`
- do not implement gateway behavior inside `menu-service`, `order-service`, `payment-service`, `inventory-service`, `kitchen-service`, or `notification-service`

Required gateway responsibilities:

- route all public endpoints to the owning service
- validate JWT for protected routes
- relay `Authorization: Bearer <token>` to downstream services when those services validate JWT
- create or forward `X-Correlation-Id`
- return `X-Correlation-Id` in the response
- apply CORS for the classroom frontend origin
- apply response security headers
- configure timeout per route
- retry only safe read requests such as `GET /api/menu/items`
- avoid blind retry for state-changing requests such as `POST /api/orders`
- log route id, status, latency, and correlation id

Required public gateway routes:

| External Route | Target Service | Owner Providing Contract | Auth Requirement | Gateway Policy |
|---|---|---|---|---|
| `GET /api/menu/items` | `menu-service` | Member 1 | public or `CUSTOMER` | safe retry allowed |
| `GET /api/menu/items/{id}` | `menu-service` | Member 1 | public or `CUSTOMER` | safe retry allowed |
| `POST /api/orders` | `order-service` | Member 2 | `CUSTOMER` | no blind retry |
| `GET /api/orders/{orderId}` | `order-service` | Member 2 | `CUSTOMER` | timeout only |
| `PATCH /api/orders/{orderId}/cancel` | `order-service` | Member 2 | `CUSTOMER` | no blind retry |
| `GET /api/kitchen/tickets` | `kitchen-service` | Member 4 | `KITCHEN_STAFF` | safe retry optional |
| `PATCH /api/kitchen/tickets/{ticketId}/status` | `kitchen-service` | Member 4 | `KITCHEN_STAFF` | no blind retry |
| `POST /api/payments/mock-confirm` | `payment-service` | Member 3 | `INSTRUCTOR` or `RESTAURANT_ADMIN` | no blind retry |

Each service owner must provide a route contract before Member 1 finalizes the gateway route.

Route contract file format:

- path and method
- target service name and internal port
- required role or public access
- request headers to forward
- whether token relay is required
- timeout target
- retry policy
- expected success and error status codes
- smoke test command through gateway

## Folder Ownership Rules

Each member owns all files under their service folders, plus service-local deployment and docs.

Member 1 owns:

- `services/gateway-service/`
- `services/menu-service/`
- `docs/contracts/gateway/`
- `docs/contracts/menu/`
- `platform/services/gateway-service/`
- `platform/services/menu-service/`

Member 2 owns:

- `services/order-service/`
- `docs/contracts/order/`
- `platform/services/order-service/`

Member 3 owns:

- `services/payment-service/`
- `services/notification-service/`
- `docs/contracts/payment/`
- `docs/contracts/notification/`
- `platform/services/payment-service/`
- `platform/services/notification-service/`

Member 4 owns:

- `services/inventory-service/`
- `services/kitchen-service/`
- `docs/contracts/inventory/`
- `docs/contracts/kitchen/`
- `platform/services/inventory-service/`
- `platform/services/kitchen-service/`

Shared folders with controlled edits:

- `docs/architecture/`
- `docs/standards/`
- `docs/contracts/events/`
- `platform/base/`
- `.github/workflows/`
- `restaurant-gitops/base/`
- `restaurant-gitops/overlays/`

Shared folder rule:

- only change shared folders during scope freeze, contract freeze, or final integration
- do not change a shared file to solve a local service problem if the fix belongs in your own service

## Member 1 Instructions

Primary mission: own the standalone API Gateway module and menu catalog access without touching order, payment, or fulfillment internals.

Owned services:

- `gateway-service`
- `menu-service`

Assignment ownership:

- Part 3 for standalone `gateway-service` and `menu-service`
- Part 4 for `GET /api/menu/items` and `GET /api/menu/items/{id}`
- Part 12 API Gateway implementation for all required public routes
- menu route exposure through gateway
- menu service Dockerfile, manifests, config, health checks, and tests
- gateway routing, JWT verification, token relay, CORS, correlation ID creation, response headers, timeout rules, retry rules, and route smoke tests

Concrete deliverables:

- `menu-service` Spring Boot scaffold
- standalone `gateway-service` Spring Cloud Gateway scaffold
- menu DTOs, controller, service, repository, validation
- gateway route definitions for all public routes
- `gateway-service` config parameters for every downstream service URL
- gateway security rules for `CUSTOMER`, `KITCHEN_STAFF`, `RESTAURANT_ADMIN`, and `INSTRUCTOR`
- CORS configuration for the classroom frontend origin
- correlation ID gateway filter or equivalent gateway configuration
- timeout and retry policy table
- token relay verification notes
- menu schema and seed data inputs for the catalog
- Dockerfiles and Kubernetes manifests for `gateway-service` and `menu-service`
- `docs/contracts/menu/menu-api.md`
- `docs/contracts/gateway/menu-route.md`
- `docs/contracts/gateway/gateway-route-table.md`
- `docs/contracts/gateway/gateway-acceptance-checks.md`

Independence rules:

- do not edit `order-service`, `payment-service`, `inventory-service`, `kitchen-service`, or `notification-service`
- implement gateway routes only from approved route contracts
- do not add business logic to the gateway
- if another member needs gateway exposure, they provide a route contract request instead of editing gateway code directly

Course focus:

- Chapter 9 API gateway, merged pages 469-538
- Chapter 5 MSA concepts and design principles, merged pages 174-263
- Chapter 6 Docker and Kubernetes, merged pages 264-383

## Member 2 Instructions

Primary mission: own the order aggregate and Saga coordination without implementing other services' internal logic.

Owned service:

- `order-service`

Assignment ownership:

- Part 2 order-side domain model and state transition definition
- Part 3 for `order-service`
- Part 4 for `POST /api/orders`, `GET /api/orders/{orderId}`, and `PATCH /api/orders/{orderId}/cancel`
- Part 6 order event publishing and Saga state tracking
- order service Dockerfile, manifests, schema, tests, and integration notes

Concrete deliverables:

- `order-service` scaffold
- order aggregate and state machine
- order schema and migrations
- order outbox event publisher
- API DTOs, validation, error handling, and cancellation rules
- event contracts for `OrderCreated.v1`, `OrderCompleted.v1`, and `OrderCancelled.v1`
- order-side Saga document describing when inventory, payment, kitchen, and notification services react
- route contract request for `/api/orders/**` under `docs/contracts/order/order-gateway-routes.md`
- Dockerfile and Kubernetes manifests for `order-service`
- `docs/contracts/order/order-api.md`
- `docs/contracts/order/order-events.md`

Independence rules:

- do not implement reservation logic in `inventory-service`
- do not implement payment authorization logic in `payment-service`
- do not implement kitchen ticket internals in `kitchen-service`
- order-service only emits commands or events and reacts to results
- do not edit `gateway-service`; provide route requirements to Member 1

Course focus:

- Chapter 9 API gateway, merged pages 469-538
- Chapter 10 MS design detail, merged pages 594-664
- Chapter 14 Saga and transaction, merged pages 789-839
- Chapter 15 microservice implementation, merged pages 840-905

## Member 3 Instructions

Primary mission: own payment and notification side effects without changing order or fulfillment service code.

Owned services:

- `payment-service`
- `notification-service`

Assignment ownership:

- Part 3 for `payment-service` and `notification-service`
- Part 4 for `POST /api/payments/mock-confirm`
- Part 5 payment and notification schema work
- Part 6 payment success, payment failure, and notification consumption
- payment and notification Dockerfiles, manifests, config, and tests

Concrete deliverables:

- `payment-service` scaffold
- `notification-service` scaffold
- mock payment confirmation API
- payment schema, refund-compensation record design, and migrations
- notification audit schema and migrations
- event contracts for `PaymentAuthorized.v1`, `PaymentFailed.v1`, `NotificationSent.v1`, and `NotificationFailed.v1`
- payment consumer or handler for order events
- notification consumer for completion or cancellation notifications
- route contract request for `/api/payments/**` under `docs/contracts/payment/payment-gateway-routes.md`
- Dockerfiles and Kubernetes manifests for `payment-service` and `notification-service`
- `docs/contracts/payment/payment-api.md`
- `docs/contracts/payment/payment-events.md`
- `docs/contracts/notification/notification-events.md`

Independence rules:

- do not update order states directly in `order-service` code
- do not reserve or release stock directly in `inventory-service`
- publish results as events and let service owners react inside their own services
- do not edit `gateway-service`; provide route requirements to Member 1

Course focus:

- Chapter 9 API gateway, merged pages 469-538
- Chapter 10 MS design detail, merged pages 594-664
- Chapter 14 Saga and transaction, merged pages 789-839
- Chapter 15 microservice implementation, merged pages 840-905

## Member 4 Instructions

Primary mission: own fulfillment logic without changing order or payment business code.

Owned services:

- `inventory-service`
- `kitchen-service`

Assignment ownership:

- Part 3 for `inventory-service` and `kitchen-service`
- Part 4 for `GET /api/kitchen/tickets` and `PATCH /api/kitchen/tickets/{ticketId}/status`
- Part 5 inventory and kitchen schema work
- Part 6 reservation, compensation release, kitchen ticket creation, and completion events
- inventory and kitchen Dockerfiles, manifests, config, and tests

Concrete deliverables:

- `inventory-service` scaffold
- `kitchen-service` scaffold
- stock reservation and release logic
- inventory schema, processed-event table, and migrations
- kitchen ticket schema and migrations
- event contracts for `InventoryReserved.v1`, `InventoryReservationFailed.v1`, `KitchenTicketCreated.v1`, and `KitchenTicketCompleted.v1`
- kitchen ticket REST endpoints and status change rules
- route contract request for `/api/kitchen/**` under `docs/contracts/kitchen/kitchen-gateway-routes.md`
- Dockerfiles and Kubernetes manifests for `inventory-service` and `kitchen-service`
- `docs/contracts/inventory/inventory-events.md`
- `docs/contracts/kitchen/kitchen-api.md`
- `docs/contracts/kitchen/kitchen-events.md`

Independence rules:

- do not edit `order-service` state transitions directly
- do not edit `payment-service` refund or failure logic directly
- keep fulfillment reactions inside inventory and kitchen service boundaries
- do not edit `gateway-service`; provide route requirements to Member 1

Course focus:

- Chapter 9 API gateway, merged pages 469-538
- Chapter 11 service separation and data design, merged pages 665-687
- Chapter 14 Saga and transaction, merged pages 789-839
- Chapter 15 microservice implementation, merged pages 840-905

## Contract-First Integration Without Overlap

To stop members from blocking each other, use these integration rules:

- Every service publishes its own API contract in `docs/contracts/<service>/`
- Every event producer publishes its own event schema in `docs/contracts/events/`
- Every consumer implements only its own consumption logic
- Gateway integration uses route contract files, not ad hoc edits across services
- Every public endpoint must have a gateway route contract before it is considered complete
- Member 1 owns the gateway module; other members own route contract inputs and route test cases for their endpoints
- Shared event names are frozen before Part 6 coding starts
- If an event changes, create a new version such as `.v2` instead of silently breaking another member's work

Use event-driven decoupling where possible because the course notes that issuers and consumers are completely independent in Pub/Sub style communication.

## API Gateway Contract Checklist

Each service owner must give Member 1 this information for every public endpoint:

- external path
- internal service target
- HTTP method
- request DTO and response DTO link
- required role
- whether anonymous access is allowed
- forwarded headers
- token relay required or not
- timeout expectation
- retry allowed or not
- fallback allowed or not
- example `curl` through gateway

Gateway route is done only when:

- route works through `gateway-service`
- correct target service receives the request
- missing token gives `401` where required
- wrong role gives `403` where required
- `Authorization` is relayed when required
- `X-Correlation-Id` is created or preserved
- no state-changing route has blind retry
- route behavior is documented in `docs/contracts/gateway/gateway-route-table.md`

## Platform and GitOps Without Service Overlap

Keep platform work service-local as much as possible:

- each member writes the Dockerfile for their own service
- each member writes the Deployment, Service, ConfigMap, and Secret reference for their own service under their own platform folder
- each member writes their own readiness and liveness probe config
- each member writes their own environment variable list

Only these items should be integrated centrally near the end:

- shared MySQL StatefulSet
- shared Kafka deployment
- shared namespace and base overlay
- shared GitHub Actions workflow
- shared ArgoCD Application
- shared Keycloak realm export
- shared Istio and observability add-ons

Central integration owner:

- Member 3 can assemble the final CI and GitOps wiring
- Member 3 should not rewrite other members' service code while doing integration
- Member 1 owns gateway service manifests and gateway runtime config
- if integration fails, the service owner fixes the problem in their own folder

Easiest CI/CD rule:

- pull requests run the GitHub Actions Maven test matrix
- merges to `main` build Docker images
- images are pushed to Docker Hub with commit SHA tags
- the GitOps repo is updated with the new SHA tag
- Argo CD syncs the Kubernetes deployment
- rollback is done by reverting the GitOps image tag commit

## Recommended Parallel Delivery Order

1. All members complete Part 0 and Part 1 and freeze standards.
2. All members complete Part 2 together, but only to define ownership, states, APIs, and event names.
3. All service owners create API contracts and gateway route contract requests for public endpoints.
4. Member 1 builds standalone `gateway-service` with placeholder routes from the frozen route contracts.
5. Member 1 builds `menu-service`.
6. Member 2 builds `order-service`.
7. Member 3 builds `payment-service` and `notification-service`.
8. Member 4 builds `inventory-service` and `kitchen-service`.
9. All members add service-local DB, Docker, manifests, and tests inside their own folders.
10. Event schemas and route contracts are reviewed once, then frozen.
11. Member 1 verifies gateway route behavior for all public endpoints with support from each endpoint owner.
12. Member 3 assembles CI, GitOps, and final deployment wiring.
13. End-to-end testing happens only after service-local work and gateway routing are stable.

## Simple Part-to-Member Mapping

| Part | Primary Owner | Notes |
|---|---|---|
| Part 0 Scope freeze | All | Freeze standards and ownership only once |
| Part 1 Repos and Agile | All | Create structure, then stop changing it casually |
| Part 2 Domain and boundaries | All | Use this step to remove future overlap |
| Part 3 Service scaffolding | Each owner for their own service lane | No cross-service edits |
| Part 4 Public REST APIs | Member 1, 2, 3, 4 by owned endpoints | Each owner implements only their endpoints |
| Part 5 Database and persistence | Each owner for their own schema | Shared MySQL only at infrastructure level |
| Part 6 Kafka, Outbox, Saga | Member 2 coordinates order flow, all implement only their side | No one codes another service's reaction |
| Part 7 Keycloak security | Shared integration phase | Apply config to owned services only |
| Part 8 Containerization | Each owner for their own services | One Dockerfile owner per service |
| Part 9 Kubernetes manifests | Each owner for service-local manifests | Shared base integrated later |
| Part 10 CI and quality gates | Member 3 assembles | Others provide service build commands |
| Part 11 GitOps with ArgoCD | Member 3 assembles | Uses service manifests from owners |
| Part 12 API gateway | Member 1 owns standalone `gateway-service` | Others provide route contracts, role rules, timeout/retry rules, and smoke tests |
| Part 13 Istio service mesh | Shared integration phase | Do not mix with business logic work |
| Part 14 Observability | Shared integration phase | Per-service logs and metrics stay local |
| Part 15 End-to-end testing | All | Failures go back to the service owner |
| Part 16 Final demo and submission | All | Build one demo from independent parts |

## No-Overlap Checklist

Before a member starts coding, confirm:

- I know exactly which service folders I own.
- I know exactly which DB schema I own.
- I know exactly which endpoints and events I own.
- I know which gateway route contracts I must provide.
- I do not need to open another member's service files to do my task.
- If I need another member, I need a contract or event, not access to their code.

## Definition of Done

This split is successful when:

- each member can work mostly inside their own service folders
- each service has one clear business role
- `gateway-service` exists as a standalone module and routes all required public endpoints
- each service owns its own schema and migrations
- service cooperation happens through APIs and events, not shared database logic
- gateway, GitOps, Istio, and observability are added through integration contracts instead of overlapping edits
- end-to-end failures can be returned to the correct service owner immediately

## Current Repo Result

This repository now contains a simple classroom baseline instead of only service shells.

Implemented in code:

- `gateway-service`: route forwarding plus a basic correlation ID filter
- `menu-service`: `GET /api/menu/items` and `GET /api/menu/items/{id}`
- `order-service`: `POST /api/orders`, `GET /api/orders/{orderId}`, and `PATCH /api/orders/{orderId}/cancel`
- `payment-service`: `POST /api/payments/mock-confirm`
- `kitchen-service`: `GET /api/kitchen/tickets` and `PATCH /api/kitchen/tickets/{ticketId}/status`
- `inventory-service`: listens to `OrderCreated` and releases stock on failure or cancellation
- `notification-service`: records simple notification attempts from business events

Implemented in event flow:

- `OrderCreated` is published from `order-service`
- `InventoryReserved` or `InventoryReservationFailed` is published from `inventory-service`
- `PaymentAuthorized` or `PaymentFailed` is published from `payment-service`
- `KitchenTicketCreated` and `KitchenTicketCompleted` are published from `kitchen-service`
- `order-service` updates simple Saga status from inventory, payment, and kitchen events
- `notification-service` writes simple notification audit records and publishes `NotificationSent`

Implemented in config:

- Kafka services use simple String serializer and deserializer config
- platform deployment images now use Docker Hub style image names
- service ConfigMaps now include the Kafka topic and consumer group values needed by the simple baseline

Current limitation:

- this is a simple direct-publish baseline, not the full Outbox relay yet
- compile verification could not be completed locally because Maven is not installed in this environment
- kitchen ticket item details are still basic and not fully copied from order items
- exception handling and validation responses still need one clean shared pass

## Next Steps To Finish The Assignment

1. Install Maven or add Maven Wrapper so the team can run `mvn test` and `mvn package` in `restaurant-app/`.
2. Add one shared error-handling style in each API service so missing records return `404` and bad input returns `400`.
3. Use the existing `outbox_event` tables for publish reliability instead of direct Kafka publish from service methods.
4. Add kitchen ticket item creation from the original order item payload so the kitchen queue shows full line-item detail.
5. Add integration tests for the public APIs and Kafka listeners service by service.
6. Add Kubernetes `Secret` objects or secret references for MySQL credentials instead of relying only on defaults.
7. Update GitHub Actions to build Docker images and update the GitOps repo with SHA tags.
8. Add Argo CD application manifests and verify `restaurant-gitops/overlays/dev` syncs the whole stack.
9. Decide whether to keep Docker Hub as the selected classroom registry or add Harbor as an advanced optional path only.
10. Run one full demo flow: menu -> order -> inventory -> payment -> kitchen -> order complete -> notification.
