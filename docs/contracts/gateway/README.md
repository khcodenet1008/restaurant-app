# Gateway Contracts

This page defines the gateway boundary, route ownership, timeout policy, and header propagation responsibility.

## Kafka Boundary

Owner: Member 1

The `gateway-service` does not publish or consume Kafka events in the first implementation. It stays focused on HTTP entry, JWT validation, correlation ID creation, timeout policy, CORS, and route forwarding.

Gateway routes must forward `X-Correlation-Id` so downstream services can copy the same value into Kafka event `traceId`.

## CI/CD Responsibility

Member 1 keeps gateway tests passing in GitHub Actions and provides the Dockerfile, ConfigMap, Deployment, Service, and GitOps image tag inputs for `gateway-service`.
