# Restaurant App Full Flow Development Work

## Scope

The project now keeps only 4 services:

- `gateway-service`
- `menu-service`
- `order-service`
- `payment-service`

## Development Flow

1. Build `gateway-service` for public entry and route forwarding.
2. Build `menu-service` for menu read APIs.
3. Build `order-service` for order create, read, and cancel APIs.
4. Build `payment-service` for mock payment confirmation.
5. Connect `order-service` and `payment-service` with simple Kafka events.
6. Build Docker images and push them with GitHub Actions.
7. Deploy through `restaurant-gitops`.
8. Optionally run the same 4 services through Istio.

## Runtime Flow

```text
Client
  -> gateway-service
  -> menu-service or order-service or payment-service

order-service
  -> order.events

payment-service
  -> payment.events

order-service
  <- payment.events
```

## Kubernetes Flow

- MySQL provides `restaurant_menu`, `restaurant_order`, and `restaurant_payment`
- Kafka provides `order.events`, `payment.events`, and `restaurant.dlq`
- GitOps overlays deploy the 4 services
- Istio overlay exposes the same 4 services through the ingress gateway
