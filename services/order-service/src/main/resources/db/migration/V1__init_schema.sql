CREATE TABLE customer_order (
  id VARCHAR(64) PRIMARY KEY,
  customer_id VARCHAR(80) NOT NULL,
  saga_id VARCHAR(64) NOT NULL,
  status VARCHAR(40) NOT NULL,
  payment_method VARCHAR(40) NOT NULL,
  subtotal_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  currency VARCHAR(3) NOT NULL DEFAULT 'USD',
  cancel_reason VARCHAR(500),
  trace_id VARCHAR(120),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_customer_order_saga_id (saga_id),
  KEY idx_customer_order_customer (customer_id),
  KEY idx_customer_order_status (status)
);

CREATE TABLE order_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id VARCHAR(64) NOT NULL,
  menu_item_id VARCHAR(64) NOT NULL,
  menu_item_name VARCHAR(160) NOT NULL,
  quantity INT NOT NULL,
  unit_price_amount DECIMAL(10, 2) NOT NULL,
  line_total_amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'USD',
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES customer_order (id),
  KEY idx_order_item_order (order_id),
  KEY idx_order_item_menu_item (menu_item_id)
);

CREATE TABLE saga_state (
  saga_id VARCHAR(64) PRIMARY KEY,
  order_id VARCHAR(64) NOT NULL,
  current_step VARCHAR(80) NOT NULL,
  status VARCHAR(40) NOT NULL,
  last_event_id VARCHAR(64),
  failure_reason VARCHAR(500),
  started_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_saga_state_order FOREIGN KEY (order_id) REFERENCES customer_order (id),
  KEY idx_saga_state_order (order_id),
  KEY idx_saga_state_status (status)
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
  UNIQUE KEY uk_order_outbox_event_id (event_id),
  KEY idx_order_outbox_status_created (status, created_at),
  KEY idx_order_outbox_aggregate (aggregate_type, aggregate_id)
);

CREATE TABLE processed_event (
  event_id VARCHAR(64) PRIMARY KEY,
  event_type VARCHAR(120) NOT NULL,
  event_version VARCHAR(20) NOT NULL DEFAULT 'v1',
  source_service VARCHAR(80) NOT NULL,
  aggregate_id VARCHAR(80),
  trace_id VARCHAR(120),
  processed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY idx_order_processed_source_type (source_service, event_type),
  KEY idx_order_processed_aggregate (aggregate_id)
);
