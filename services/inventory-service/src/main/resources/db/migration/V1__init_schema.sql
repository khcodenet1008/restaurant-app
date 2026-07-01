CREATE TABLE inventory_item (
  id VARCHAR(64) PRIMARY KEY,
  menu_item_id VARCHAR(64) NOT NULL,
  item_name VARCHAR(160) NOT NULL,
  available_quantity INT NOT NULL DEFAULT 0,
  reserved_quantity INT NOT NULL DEFAULT 0,
  reorder_threshold INT NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_inventory_menu_item (menu_item_id)
);

CREATE TABLE stock_reservation (
  id VARCHAR(64) PRIMARY KEY,
  order_id VARCHAR(64) NOT NULL,
  saga_id VARCHAR(64) NOT NULL,
  status VARCHAR(40) NOT NULL,
  trace_id VARCHAR(120),
  failure_reason VARCHAR(500),
  expires_at TIMESTAMP(6) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_stock_reservation_order (order_id),
  KEY idx_stock_reservation_saga (saga_id),
  KEY idx_stock_reservation_status (status)
);

CREATE TABLE stock_reservation_line (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id VARCHAR(64) NOT NULL,
  inventory_item_id VARCHAR(64) NOT NULL,
  menu_item_id VARCHAR(64) NOT NULL,
  requested_quantity INT NOT NULL,
  reserved_quantity INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_reservation_line_reservation FOREIGN KEY (reservation_id) REFERENCES stock_reservation (id),
  CONSTRAINT fk_reservation_line_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_item (id),
  KEY idx_reservation_line_reservation (reservation_id),
  KEY idx_reservation_line_menu_item (menu_item_id)
);

CREATE TABLE outbox_event (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id VARCHAR(64) NOT NULL,
  aggregate_type VARCHAR(80) NOT NULL,
  aggregate_id VARCHAR(80) NOT NULL,
  event_type VARCHAR(120) NOT NULL,
  event_version VARCHAR(20) NOT NULL DEFAULT 'v1',
  topic_name VARCHAR(120) NOT NULL,
  trace_id VARCHAR(120),
  payload JSON NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  published_at TIMESTAMP(6) NULL,
  UNIQUE KEY uk_inventory_outbox_event_id (event_id),
  KEY idx_inventory_outbox_status_created (status, created_at),
  KEY idx_inventory_outbox_aggregate (aggregate_type, aggregate_id)
);

CREATE TABLE processed_event (
  event_id VARCHAR(64) PRIMARY KEY,
  event_type VARCHAR(120) NOT NULL,
  event_version VARCHAR(20) NOT NULL DEFAULT 'v1',
  source_service VARCHAR(80) NOT NULL,
  aggregate_id VARCHAR(80),
  trace_id VARCHAR(120),
  processed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY idx_inventory_processed_source_type (source_service, event_type),
  KEY idx_inventory_processed_aggregate (aggregate_id)
);

INSERT INTO inventory_item (id, menu_item_id, item_name, available_quantity, reserved_quantity, reorder_threshold) VALUES
  ('inv-ramen-01', 'ramen-01', 'Class Demo Ramen', 100, 0, 10),
  ('inv-tea-01', 'tea-01', 'Iced Tea', 100, 0, 10);
