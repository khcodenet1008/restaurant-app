CREATE TABLE notification_attempt (
  id VARCHAR(64) PRIMARY KEY,
  order_id VARCHAR(64),
  customer_id VARCHAR(80),
  channel VARCHAR(40) NOT NULL,
  notification_type VARCHAR(80) NOT NULL,
  destination VARCHAR(255),
  status VARCHAR(40) NOT NULL,
  message VARCHAR(1000),
  failure_reason VARCHAR(500),
  trace_id VARCHAR(120),
  requested_event_id VARCHAR(64),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  sent_at TIMESTAMP(6) NULL,
  KEY idx_notification_order (order_id),
  KEY idx_notification_customer (customer_id),
  KEY idx_notification_status (status),
  KEY idx_notification_requested_event (requested_event_id)
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
  UNIQUE KEY uk_notification_outbox_event_id (event_id),
  KEY idx_notification_outbox_status_created (status, created_at),
  KEY idx_notification_outbox_aggregate (aggregate_type, aggregate_id)
);

CREATE TABLE processed_event (
  event_id VARCHAR(64) PRIMARY KEY,
  event_type VARCHAR(120) NOT NULL,
  event_version VARCHAR(20) NOT NULL DEFAULT 'v1',
  source_service VARCHAR(80) NOT NULL,
  aggregate_id VARCHAR(80),
  trace_id VARCHAR(120),
  processed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY idx_notification_processed_source_type (source_service, event_type),
  KEY idx_notification_processed_aggregate (aggregate_id)
);
