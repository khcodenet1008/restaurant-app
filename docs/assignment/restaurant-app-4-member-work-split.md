# Restaurant App Assignment Split for 4 Members

This assignment now keeps only 4 services:

- `gateway-service`
- `menu-service`
- `order-service`
- `payment-service`

## Team Split

- Member 1: `gateway-service`
- Member 2: `menu-service`
- Member 3: `order-service`
- Member 4: `payment-service`

## Shared Standards

- package naming: `com.example.restaurant.<service>`
- correlation header: `X-Correlation-Id`
- gateway implementation: Spring Cloud Gateway
- Kafka topics: `order.events`, `payment.events`, `restaurant.dlq`
- CI/CD path: GitHub Actions, Docker Hub, `restaurant-gitops`, and Argo CD style sync

## Required Public Routes

| Route | Target Service |
|---|---|
| `GET /api/menu/items` | `menu-service` |
| `GET /api/menu/items/{id}` | `menu-service` |
| `POST /api/orders` | `order-service` |
| `GET /api/orders/{orderId}` | `order-service` |
| `PATCH /api/orders/{orderId}/cancel` | `order-service` |
| `POST /api/payments/mock-confirm` | `payment-service` |

## Simple Event Flow

```text
order-service -> order.events
payment-service -> payment.events
order-service <- payment.events
```

## Folder Ownership

- Member 1: `services/gateway-service/`, `platform/services/gateway-service/`
- Member 2: `services/menu-service/`, `platform/services/menu-service/`
- Member 3: `services/order-service/`, `platform/services/order-service/`
- Member 4: `services/payment-service/`, `platform/services/payment-service/`
