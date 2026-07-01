# MySQL Init SQL

`00-create-databases.sql` is a demo bootstrap template for creating the service-owned schemas and `restaurant_app` user.

Use this script only as infrastructure setup. Business tables are owned by service-local Flyway migrations under:

- `services/menu-service/src/main/resources/db/migration`
- `services/order-service/src/main/resources/db/migration`
- `services/payment-service/src/main/resources/db/migration`
- `services/notification-service/src/main/resources/db/migration`
- `services/inventory-service/src/main/resources/db/migration`
- `services/kitchen-service/src/main/resources/db/migration`
