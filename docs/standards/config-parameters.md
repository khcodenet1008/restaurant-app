# Configuration Parameter Reference

This file documents which parameters belong to which service and where they should be configured.

## Configuration Sources

- Local development: `.env`, shell environment, or IDE run config
- Service defaults: `services/<service>/src/main/resources/application.yml`
- Kubernetes non-secret config: `ConfigMap`
- Kubernetes secret config: `Secret`
- GitOps environment overrides: `restaurant-gitops/overlays/<env>/`

## Shared Parameters

| Parameter | Example | Owner | Secret | Used By |
|---|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | Service owner | No | All services |
| `SERVER_PORT` | `8080` | Service owner | No | All services |
| `MANAGEMENT_SERVER_PORT` | `8081` | Service owner | No | All services |
| `LOG_LEVEL_ROOT` | `INFO` | Service owner | No | All services |
| `CORRELATION_HEADER_NAME` | `X-Correlation-Id` | Shared standard | No | All services |
| `KEYCLOAK_ISSUER_URI` | `http://keycloak:8080/realms/restaurant-demo` | Shared integration | No | Gateway and protected services |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Shared integration | No | Order, payment, notification, inventory, kitchen |
| `MYSQL_HOST` | `mysql` | Shared integration | No | Menu, order, payment, notification, inventory, kitchen |
| `MYSQL_PORT` | `3306` | Shared integration | No | Menu, order, payment, notification, inventory, kitchen |
| `MYSQL_USERNAME` | `restaurant_app` | Shared integration | Yes | Menu, order, payment, notification, inventory, kitchen |
| `MYSQL_PASSWORD` | `change-me` | Shared integration | Yes | Menu, order, payment, notification, inventory, kitchen |

## Schema Ownership

| Schema | Owner Service |
|---|---|
| `restaurant_menu` | `menu-service` |
| `restaurant_order` | `order-service` |
| `restaurant_payment` | `payment-service` |
| `restaurant_notification` | `notification-service` |
| `restaurant_inventory` | `inventory-service` |
| `restaurant_kitchen` | `kitchen-service` |

## Service Parameters

### gateway-service

| Parameter | Example | Secret | Notes |
|---|---|---|---|
| `GATEWAY_PORT` | `8080` | No | External HTTP entry point |
| `MENU_SERVICE_URL` | `http://menu-service:8080` | No | Route target |
| `ORDER_SERVICE_URL` | `http://order-service:8080` | No | Route target |
| `PAYMENT_SERVICE_URL` | `http://payment-service:8080` | No | Route target |
| `KITCHEN_SERVICE_URL` | `http://kitchen-service:8080` | No | Route target |
| `GATEWAY_CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | No | Frontend origin list |
| `GATEWAY_TIMEOUT_MS` | `2000` | No | Safe default for upstream timeouts |

### menu-service

| Parameter | Example | Secret | Notes |
|---|---|---|---|
| `MENU_DB_NAME` | `restaurant_menu` | No | Service-owned schema |
| `MENU_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_menu` | No | Preferred full JDBC override |
| `MENU_SEED_ENABLED` | `true` | No | Demo seed toggle |

### order-service

| Parameter | Example | Secret | Notes |
|---|---|---|---|
| `ORDER_DB_NAME` | `restaurant_order` | No | Service-owned schema |
| `ORDER_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_order` | No | Preferred full JDBC override |
| `ORDER_EVENTS_TOPIC` | `order.events` | No | Order event topic |
| `ORDER_COMMAND_TIMEOUT_MS` | `5000` | No | Saga wait or retry coordination setting |

### payment-service

| Parameter | Example | Secret | Notes |
|---|---|---|---|
| `PAYMENT_DB_NAME` | `restaurant_payment` | No | Service-owned schema |
| `PAYMENT_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_payment` | No | Preferred full JDBC override |
| `PAYMENT_EVENTS_TOPIC` | `payment.events` | No | Payment event topic |
| `PAYMENT_MOCK_MODE` | `true` | No | Keep real provider out of scope |

### notification-service

| Parameter | Example | Secret | Notes |
|---|---|---|---|
| `NOTIFICATION_DB_NAME` | `restaurant_notification` | No | Service-owned schema |
| `NOTIFICATION_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_notification` | No | Preferred full JDBC override |
| `NOTIFICATION_EVENTS_TOPIC` | `notification.events` | No | Notification event topic |
| `NOTIFICATION_CHANNELS` | `EMAIL,SMS,APP` | No | Demo channel list |

### inventory-service

| Parameter | Example | Secret | Notes |
|---|---|---|---|
| `INVENTORY_DB_NAME` | `restaurant_inventory` | No | Service-owned schema |
| `INVENTORY_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_inventory` | No | Preferred full JDBC override |
| `INVENTORY_EVENTS_TOPIC` | `inventory.events` | No | Inventory event topic |
| `INVENTORY_RESERVATION_TTL_SECONDS` | `900` | No | Demo reservation window |

### kitchen-service

| Parameter | Example | Secret | Notes |
|---|---|---|---|
| `KITCHEN_DB_NAME` | `restaurant_kitchen` | No | Service-owned schema |
| `KITCHEN_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_kitchen` | No | Preferred full JDBC override |
| `KITCHEN_EVENTS_TOPIC` | `kitchen.events` | No | Kitchen event topic |
| `KITCHEN_DEFAULT_QUEUE` | `main-line` | No | Demo ticket queue |

## Where to Put Values

| Kind of Value | Put It In |
|---|---|
| Shared non-secret runtime values | `platform/services/<service>/configmap.yaml` |
| Passwords and client secrets | Kubernetes `Secret` and secret manager later |
| Local developer overrides | Shell env or `.env` not committed |
| Environment-specific overrides | `restaurant-gitops/overlays/dev/` and `restaurant-gitops/overlays/demo/` |

## Config Ownership Rule

If a parameter changes the business behavior of one service, that service owner owns it. If a parameter affects only shared infrastructure, the integration phase owns it.
