# Shared Standards

Freeze these standards before deep implementation:

- Package naming: `com.example.restaurant.<service>`
- Error body: `code`, `message`, `traceId`, `timestamp`
- Correlation header: `X-Correlation-Id`
- Event envelope: `eventId`, `eventType`, `eventVersion`, `sagaId`, `aggregateId`, `occurredAt`, `source`, `traceId`, `payload`
- Default Java version: `17`
- Default Spring profile key: `SPRING_PROFILES_ACTIVE`
- Per-service schema ownership only
- No direct cross-service database reads or writes
- Kafka message key: use `orderId` for order Saga events
- Kafka topics: `order.events`, `payment.events`, `restaurant.dlq`
- CI/CD path: GitHub Actions, Docker Hub SHA image tags, GitOps manifests, Argo CD sync
- Istio demo scope: `gateway-service`, `menu-service`, `order-service`, and `payment-service` only for the first Chapter 12 mesh pass

## Folder Rules

- `services/<service>/`: service owner only
- `docs/contracts/<service>/`: service owner only
- `platform/services/<service>/`: service owner only
- `docs/contracts/events/`, `platform/base/`, and GitOps overlays: shared integration phase only

## Kafka Rules

- Publish facts that already happened, such as `OrderCreated.v1`.
- Do not publish database table rows directly as event payloads.
- Include `traceId` and `sagaId` in every Saga event.
- Send events that still fail after retries to `restaurant.dlq`.
- Keep the current flow basic: `order-service` publishes order facts and `payment-service` publishes payment result facts.

## CI/CD Rules

- Pull requests must pass the GitHub Actions Maven test matrix.
- Main branch builds should use immutable commit SHA image tags.
- `latest` can exist for convenience, but GitOps deployments should point to SHA tags.
- Rollback should be done by reverting the GitOps image tag commit.
