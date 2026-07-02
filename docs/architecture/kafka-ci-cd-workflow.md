# Kafka and CI/CD Workflow

This assignment uses the easiest course-aligned path: direct Kafka events between services and a GitHub Actions plus GitOps delivery workflow.

Course anchors:

- Chapter 7 DevOps Concept and Practice, merged pages 394-420: CI, CD, GitOps, GitHub Actions, Docker image build, registry push, GitOps manifest update, and Argo CD sync.
- Chapter 14 Saga and Transaction, merged pages 789-839: Kafka event chain, success/failure events, compensation, idempotency, and DLQ.
- Chapter 15 Microservice Implementation, merged pages 840-905: Spring event/message implementation, Outbox, processed events, and retry testing.

## Decision

| Topic | Chosen Option | Why This Is Easiest |
|---|---|---|
| Message broker | Kafka | Matches the course Saga chapter and current platform skeleton |
| Kafka setup | Single-node demo Kafka in Kubernetes | Enough for local and classroom demonstration |
| Event integration | Spring `KafkaTemplate` and `@KafkaListener` | Simpler than CDC or Debezium for this assignment |
| Transaction safety | Local DB transaction plus `outbox_event` table | Keeps service database changes and event intent together |
| Duplicate protection | `processed_event` table in consumers | Required because Kafka consumers can receive duplicates |
| CI tool | GitHub Actions | Course-recommended and already present in `.github/workflows/ci.yml` |
| Image registry | Docker Hub | Simplest classroom registry; Harbor can replace it later |
| CD workflow | GitOps with Argo CD | Matches the course GitOps practice and existing `restaurant-gitops/` folder |
| Manifest management | Kustomize base plus overlays | Already present and easy to review |

## Kafka Event Flow

Use choreography for the first implementation. Each service reacts to facts from the previous service and publishes its own result.

```text
POST /api/orders
  -> order-service saves order
  -> OrderCreated.v1 on order.events
  -> inventory-service reserves stock
  -> InventoryReserved.v1 or InventoryReservationFailed.v1 on inventory.events
  -> payment-service authorizes mock payment
  -> PaymentAuthorized.v1 or PaymentFailed.v1 on payment.events
  -> kitchen-service creates kitchen ticket
  -> KitchenTicketCreated.v1 and KitchenTicketCompleted.v1 on kitchen.events
  -> order-service completes or cancels order
  -> notification-service records notification attempts
```

Failure flow:

```text
InventoryReservationFailed.v1
  -> order-service marks order CANCELLED
  -> notification-service sends failure notice

PaymentFailed.v1
  -> inventory-service releases reservation
  -> order-service marks order CANCELLED
  -> notification-service sends failure notice
```

## Topics

| Topic | Producer | Main Consumers | Purpose |
|---|---|---|---|
| `order.events` | `order-service` | `inventory-service`, `notification-service` | Order creation, completion, and cancellation facts |
| `inventory.events` | `inventory-service` | `payment-service`, `order-service`, `notification-service` | Reservation success, failure, and release facts |
| `payment.events` | `payment-service` | `kitchen-service`, `inventory-service`, `order-service`, `notification-service` | Payment approval or failure facts |
| `kitchen.events` | `kitchen-service` | `order-service`, `notification-service` | Kitchen ticket lifecycle facts |
| `notification.events` | `notification-service` | optional audit consumers | Notification delivery facts |
| `restaurant.dlq` | all event consumers | integration team | Failed event processing after retries |

Use `orderId` as the Kafka message key for all order-related events so events for the same order keep stable partition ordering.

## Event Envelope

All Kafka messages use the shared event envelope from `docs/standards/shared-standards.md`.

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "eventVersion": "v1",
  "sagaId": "uuid",
  "aggregateId": "order-1001",
  "occurredAt": "2026-07-02T10:00:00Z",
  "source": "order-service",
  "traceId": "trace-id",
  "payload": {}
}
```

Rules:

- Event names describe completed facts, such as `OrderCreated.v1`, not commands.
- Each consumer stores `eventId` in `processed_event` before or during successful handling.
- A service must not publish the same event type again in response to receiving that same event type.
- Failed messages retry with backoff and then move to `restaurant.dlq`.
- Payloads include only data needed by other services, not entire database rows.

## Outbox Rule

Any service that changes local business state and needs to publish an event writes both changes in one local database transaction:

```text
business table update
outbox_event insert
commit
outbox relay publishes pending event to Kafka
mark outbox_event as PUBLISHED
```

For this assignment, the relay can be a scheduled Spring component inside each service. Debezium and Kafka Connect are useful later, but direct relay is easier for the first classroom version.

## CI Workflow

The existing `.github/workflows/ci.yml` is the first CI gate.

Pull request flow:

```text
Developer push
  -> GitHub Actions
  -> Maven test matrix for all 7 services
  -> PR can be reviewed
```

Main branch flow:

```text
Merge to main
  -> Maven test
  -> Docker build per changed service
  -> Push image to Docker Hub with SHA tag
  -> Update image tag in restaurant-gitops
  -> Commit GitOps change
```

Use SHA tags for deployable images. The course warns that `latest` is hard to trace and harder to roll back, while SHA tags give better reproducibility and rollback.

Required GitHub secrets:

| Secret | Purpose |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `GITOPS_TOKEN` | Token that allows the app workflow to update the GitOps repo |

## CD Workflow

Argo CD watches the GitOps desired state and syncs Kubernetes when manifests change.

```text
restaurant-gitops commit
  -> Argo CD detects drift
  -> Argo CD syncs `overlays/dev` or `overlays/demo`
  -> Kubernetes rolls out the new image
```

For classroom use, keep deployment manual approval simple:

- Pull requests run CI only.
- Merges to `main` can build and push images.
- Promotion happens when the GitOps image tag is updated.
- Rollback means reverting the GitOps commit that changed the image tag.

## Team Responsibilities

| Owner | Kafka Responsibility | CI/CD Responsibility |
|---|---|---|
| Member 1 | Gateway forwards REST requests; menu can publish optional menu availability events later | Keep gateway and menu tests passing |
| Member 2 | Own `order.events` and Saga state transitions | Provide order service Dockerfile and manifest inputs |
| Member 3 | Own `payment.events`, `notification.events`, and final workflow assembly | Assemble GitHub Actions, Docker image naming, and GitOps promotion |
| Member 4 | Own `inventory.events` and `kitchen.events` | Provide inventory and kitchen manifest inputs |

## Verification Checklist

- `kubectl get pods -n restaurant-demo` shows Kafka, MySQL, and service pods running.
- `kubectl get svc -n restaurant-demo` shows internal service DNS names.
- Each service starts with `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`.
- `POST /api/orders` creates an `OrderCreated.v1` event in `order.events`.
- Duplicate event delivery does not create duplicate reservations, payments, tickets, or notifications.
- A simulated payment failure releases inventory and cancels the order.
- GitHub Actions passes Maven tests before merge.
- GitOps image tags use commit SHA values.
- Argo CD shows the application as synced and healthy after promotion.
