CREATE TABLE menu_category (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  display_order INT NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE menu_item (
  id VARCHAR(64) PRIMARY KEY,
  category_id VARCHAR(64) NOT NULL,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(500),
  price_amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'USD',
  image_url VARCHAR(500),
  available BOOLEAN NOT NULL DEFAULT TRUE,
  allergen_labels JSON,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_menu_item_category FOREIGN KEY (category_id) REFERENCES menu_category (id)
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
  UNIQUE KEY uk_menu_outbox_event_id (event_id),
  KEY idx_menu_outbox_status_created (status, created_at),
  KEY idx_menu_outbox_aggregate (aggregate_type, aggregate_id)
);

INSERT INTO menu_category (id, name, display_order) VALUES
  ('cat-noodles', 'Noodles', 1),
  ('cat-drinks', 'Drinks', 2);

INSERT INTO menu_item (id, category_id, name, description, price_amount, currency, image_url, available, allergen_labels) VALUES
  ('ramen-01', 'cat-noodles', 'Class Demo Ramen', 'Warm noodle bowl for the demo order flow.', 6.50, 'USD', '/images/menu/ramen-01.jpg', TRUE, JSON_ARRAY('gluten', 'soy')),
  ('tea-01', 'cat-drinks', 'Iced Tea', 'Simple drink item for bundled demo orders.', 1.50, 'USD', '/images/menu/tea-01.jpg', TRUE, JSON_ARRAY());
