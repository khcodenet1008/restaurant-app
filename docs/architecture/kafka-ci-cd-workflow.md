# Kafka and CI/CD Workflow

This repository now uses a simplified 4-service workflow:

- `gateway-service`
- `menu-service`
- `order-service`
- `payment-service`

## Runtime Path

Synchronous HTTP path:

```text
Client
  -> gateway-service:8080
  -> menu-service:8080 or order-service:8080 or payment-service:8080
```

Asynchronous Kafka path:

```text
order-service
  -> order.events

payment-service
  -> payment.events

order-service
  <- payment.events
```

Shared platform endpoints:

- MySQL: `mysql:3306`
- Kafka: `kafka:9092`

See [system-communication-graph.md](system-communication-graph.md) for the full graph.

## Simplified Business Flow

```text
POST /api/orders
  -> gateway-service forwards to order-service
  -> order-service stores order
  -> order-service publishes OrderCreated.v1

POST /api/payments/mock-confirm
  -> gateway-service forwards to payment-service
  -> payment-service stores payment result
  -> payment-service publishes PaymentAuthorized.v1 or PaymentFailed.v1
  -> order-service consumes payment.events
  -> order-service marks order COMPLETED or CANCELLED
```

## Topics

| Topic | Producer | Consumer | Purpose |
|---|---|---|---|
| `order.events` | `order-service` | optional future consumers | Order facts |
| `payment.events` | `payment-service` | `order-service` | Payment result updates |
| `restaurant.dlq` | consumers | integration team | Failed retries |

## CI Flow

```text
Developer push
  -> GitHub Actions
  -> Maven build and test
  -> Docker image build
  -> Docker Hub push with SHA tag
  -> GitOps manifest update
```

## CD Flow

```text
restaurant-gitops change
  -> Argo CD or kubectl apply -k
  -> Kubernetes rollout
  -> gateway-service, menu-service, order-service, payment-service run in cluster
```

## Istio Relationship

The Istio demo keeps the same 4 services. It only changes the HTTP entry path:

```text
Client
  -> Istio IngressGateway:80
  -> gateway-service:8080
  -> menu-service / order-service / payment-service
```

Kafka and GitOps remain the same in the Istio overlay.

## Verification Checklist

- `kubectl get pods -n restaurant-demo`
- `kubectl get svc -n restaurant-demo`
- `kubectl logs deployment/gateway-service -n restaurant-demo`
- `kubectl logs deployment/order-service -n restaurant-demo`
- `kubectl logs deployment/payment-service -n restaurant-demo`
