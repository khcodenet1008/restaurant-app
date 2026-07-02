-- Demo bootstrap for the restaurant assignment.
-- Run once as a MySQL admin user before service-local Flyway migrations.

CREATE DATABASE IF NOT EXISTS restaurant_menu
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS restaurant_order
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS restaurant_payment
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'restaurant_app'@'%' IDENTIFIED BY 'change-me';

GRANT ALL PRIVILEGES ON restaurant_menu.* TO 'restaurant_app'@'%';
GRANT ALL PRIVILEGES ON restaurant_order.* TO 'restaurant_app'@'%';
GRANT ALL PRIVILEGES ON restaurant_payment.* TO 'restaurant_app'@'%';

FLUSH PRIVILEGES;
