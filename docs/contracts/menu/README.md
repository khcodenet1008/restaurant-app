# Menu Contracts

This page defines menu API ownership and menu response contract responsibilities.

## Kafka Boundary

Owner: Member 1

The `menu-service` is read-heavy and does not participate in the first order Saga event chain. It may publish menu availability events later, but the easiest assignment path keeps menu access synchronous through the gateway.

## CI/CD Responsibility

Member 1 keeps menu tests passing in GitHub Actions and provides the Dockerfile, ConfigMap, Deployment, Service, and GitOps image tag inputs for `menu-service`.
