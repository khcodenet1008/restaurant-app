# Database SQL Templates

These SQL templates follow the assignment rule: each service owns its own schema and must not read or write another service database directly.

Course anchors:

- Chapter 11 MSA service separation and data design, merged pages 665-675: divide by business responsibility, each service manages only its own data, and shared databases are banned for service logic.
- Chapter 14 Saga and transaction, merged pages 789-839: multi-service state changes use events, Saga, compensation, outbox, and idempotent consumers.

## Files

| File | Purpose |
|---|---|
| `platform/base/mysql-init/00-create-databases.sql` | Demo MySQL bootstrap for schemas and user grants |
| `services/menu-service/src/main/resources/db/migration/V1__init_schema.sql` | Menu service schema |
| `services/order-service/src/main/resources/db/migration/V1__init_schema.sql` | Order and Saga schema |
| `services/payment-service/src/main/resources/db/migration/V1__init_schema.sql` | Payment and refund schema |
| `services/notification-service/src/main/resources/db/migration/V1__init_schema.sql` | Notification audit schema |
| `services/inventory-service/src/main/resources/db/migration/V1__init_schema.sql` | Stock and reservation schema |
| `services/kitchen-service/src/main/resources/db/migration/V1__init_schema.sql` | Kitchen ticket schema |

## Ownership

| Schema | Owner |
|---|---|
| `restaurant_menu` | Member 1, `menu-service` |
| `restaurant_order` | Member 2, `order-service` |
| `restaurant_payment` | Member 3, `payment-service` |
| `restaurant_notification` | Member 3, `notification-service` |
| `restaurant_inventory` | Member 4, `inventory-service` |
| `restaurant_kitchen` | Member 4, `kitchen-service` |

## Shared Event Tables

Most services include:

- `outbox_event`: stores events in the same local transaction as business state changes.
- `processed_event`: stores consumed event IDs to make consumers idempotent.

Menu currently has `outbox_event` for future catalog availability events but does not need `processed_event` unless it starts consuming events.

The first Kafka implementation uses an application-level Outbox relay in each publishing service. Debezium and Kafka Connect are optional later improvements, but the direct relay is the easiest assignment path.

## Usage

1. Run `platform/base/mysql-init/00-create-databases.sql` once for a local/demo MySQL instance.
2. Let each service run its own Flyway migration from `src/main/resources/db/migration`.
3. Keep later changes service-local by adding `V2__...sql`, `V3__...sql`, and so on inside the owning service folder.

## Boundary Rule

Do not create foreign keys across schemas. Cross-service references such as `order_id`, `menu_item_id`, or `payment_id` are stored as business identifiers only. Data synchronization happens through APIs or events, not direct database joins.

## Kafka and CI/CD Rule

Schema changes must pass the GitHub Actions Maven test matrix before merge. If a schema change affects an event payload, update the matching event contract in `docs/contracts/events/README.md` and the owning service contract before promotion through GitOps.
