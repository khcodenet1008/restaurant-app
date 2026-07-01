CREATE TABLE payment_authorization (
  id VARCHAR(64) PRIMARY KEY,
  order_id VARCHAR(64) NOT NULL,
  saga_id VARCHAR(64) NOT NULL,
  customer_id VARCHAR(80) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'USD',
  payment_method VARCHAR(40) NOT NULL,
  status VARCHAR(40) NOT NULL,
  mock_result VARCHAR(40),
  failure_reason VARCHAR(500),
  trace_id VARCHAR(120),
  authorized_at TIMESTAMP(6) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_payment_order_id (order_id),
  KEY idx_payment_saga_id (saga_id),
  KEY idx_payment_status (status)
);

CREATE TABLE payment_refund (
  id VARCHAR(64) PRIMARY KEY,
  payment_authorization_id VARCHAR(64) NOT NULL,
  order_id VARCHAR(64) NOT NULL,
  saga_id VARCHAR(64) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'USD',
  status VARCHAR(40) NOT NULL,
  reason VARCHAR(500),
  trace_id VARCHAR(120),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_payment_refund_authorization FOREIGN KEY (payment_authorization_id) REFERENCES payment_authorization (id),
  KEY idx_payment_refund_order (order_id),
  KEY idx_payment_refund_status (status)
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
  UNIQUE KEY uk_payment_outbox_event_id (event_id),
  KEY idx_payment_outbox_status_created (status, created_at),
  KEY idx_payment_outbox_aggregate (aggregate_type, aggregate_id)
);

CREATE TABLE processed_event (
  event_id VARCHAR(64) PRIMARY KEY,
  event_type VARCHAR(120) NOT NULL,
  event_version VARCHAR(20) NOT NULL DEFAULT 'v1',
  source_service VARCHAR(80) NOT NULL,
  aggregate_id VARCHAR(80),
  trace_id VARCHAR(120),
  processed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY idx_payment_processed_source_type (source_service, event_type),
  KEY idx_payment_processed_aggregate (aggregate_id)
);
