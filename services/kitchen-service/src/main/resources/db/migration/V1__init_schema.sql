CREATE TABLE kitchen_ticket (
  id VARCHAR(64) PRIMARY KEY,
  order_id VARCHAR(64) NOT NULL,
  saga_id VARCHAR(64) NOT NULL,
  queue_name VARCHAR(80) NOT NULL DEFAULT 'main-line',
  status VARCHAR(40) NOT NULL,
  priority INT NOT NULL DEFAULT 0,
  assigned_to VARCHAR(80),
  trace_id VARCHAR(120),
  failure_reason VARCHAR(500),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  accepted_at TIMESTAMP(6) NULL,
  completed_at TIMESTAMP(6) NULL,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_kitchen_ticket_order (order_id),
  KEY idx_kitchen_ticket_saga (saga_id),
  KEY idx_kitchen_ticket_status (status),
  KEY idx_kitchen_ticket_queue (queue_name)
);

CREATE TABLE kitchen_ticket_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ticket_id VARCHAR(64) NOT NULL,
  menu_item_id VARCHAR(64) NOT NULL,
  menu_item_name VARCHAR(160) NOT NULL,
  quantity INT NOT NULL,
  special_instruction VARCHAR(500),
  CONSTRAINT fk_ticket_item_ticket FOREIGN KEY (ticket_id) REFERENCES kitchen_ticket (id),
  KEY idx_ticket_item_ticket (ticket_id),
  KEY idx_ticket_item_menu_item (menu_item_id)
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
  UNIQUE KEY uk_kitchen_outbox_event_id (event_id),
  KEY idx_kitchen_outbox_status_created (status, created_at),
  KEY idx_kitchen_outbox_aggregate (aggregate_type, aggregate_id)
);

CREATE TABLE processed_event (
  event_id VARCHAR(64) PRIMARY KEY,
  event_type VARCHAR(120) NOT NULL,
  event_version VARCHAR(20) NOT NULL DEFAULT 'v1',
  source_service VARCHAR(80) NOT NULL,
  aggregate_id VARCHAR(80),
  trace_id VARCHAR(120),
  processed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY idx_kitchen_processed_source_type (source_service, event_type),
  KEY idx_kitchen_processed_aggregate (aggregate_id)
);
