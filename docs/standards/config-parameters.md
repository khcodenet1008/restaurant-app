# Configuration Parameter Reference

This file documents the active configuration for the simplified 4-service system.

## Shared Parameters

| Parameter | Example | Used By |
|---|---|---|
| `SERVER_PORT` | `8080` | all services |
| `MANAGEMENT_SERVER_PORT` | `8081` | all services |
| `KEYCLOAK_ISSUER_URI` | `http://keycloak:8080/realms/restaurant-demo` | gateway and protected services |
| `MYSQL_HOST` | `mysql` | menu, order, payment |
| `MYSQL_PORT` | `3306` | menu, order, payment |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | order, payment |

## Schema Ownership

| Schema | Owner Service |
|---|---|
| `restaurant_menu` | `menu-service` |
| `restaurant_order` | `order-service` |
| `restaurant_payment` | `payment-service` |

## Service Parameters

### gateway-service

| Parameter | Example | Notes |
|---|---|---|
| `GATEWAY_PORT` | `8080` | External HTTP entry point |
| `MENU_SERVICE_URL` | `http://menu-service:8080` | Route target |
| `ORDER_SERVICE_URL` | `http://order-service:8080` | Route target |
| `PAYMENT_SERVICE_URL` | `http://payment-service:8080` | Route target |

### menu-service

| Parameter | Example | Notes |
|---|---|---|
| `MENU_DB_NAME` | `restaurant_menu` | Service-owned schema |
| `MENU_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_menu` | Full JDBC override |

### order-service

| Parameter | Example | Notes |
|---|---|---|
| `ORDER_DB_NAME` | `restaurant_order` | Service-owned schema |
| `ORDER_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_order` | Full JDBC override |
| `ORDER_EVENTS_TOPIC` | `order.events` | Published order topic |
| `PAYMENT_EVENTS_TOPIC` | `payment.events` | Consumed payment result topic |
| `ORDER_CONSUMER_GROUP` | `order-service` | Consumer group for payment events |

### payment-service

| Parameter | Example | Notes |
|---|---|---|
| `PAYMENT_DB_NAME` | `restaurant_payment` | Service-owned schema |
| `PAYMENT_DB_URL` | `jdbc:mysql://mysql:3306/restaurant_payment` | Full JDBC override |
| `PAYMENT_EVENTS_TOPIC` | `payment.events` | Published payment topic |
| `PAYMENT_CONSUMER_GROUP` | `payment-service` | Reserved for future consumers |
| `PAYMENT_MOCK_MODE` | `true` | Classroom mock provider toggle |
