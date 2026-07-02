# Database SQL Templates

The simplified system keeps one schema per remaining service.

## Files

| File | Purpose |
|---|---|
| `platform/base/mysql-init/00-create-databases.sql` | Demo MySQL bootstrap for schemas and user grants |
| `services/menu-service/src/main/resources/db/migration/V1__init_schema.sql` | Menu schema |
| `services/order-service/src/main/resources/db/migration/V1__init_schema.sql` | Order schema |
| `services/payment-service/src/main/resources/db/migration/V1__init_schema.sql` | Payment schema |

## Ownership

| Schema | Owner |
|---|---|
| `restaurant_menu` | `menu-service` |
| `restaurant_order` | `order-service` |
| `restaurant_payment` | `payment-service` |

## Rule

- one service owns one schema
- no cross-schema business writes
- schema changes should be kept inside the owning service migration folder
