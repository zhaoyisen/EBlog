ALTER TABLE posts
  ADD COLUMN moderation_reason VARCHAR(255) NULL,
  ADD COLUMN moderation_updated_at TIMESTAMP NULL;

CREATE TABLE IF NOT EXISTS moderation_outbox (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(32) NOT NULL,
  entity_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  attempts INT NOT NULL DEFAULT 0,
  next_run_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  locked_at TIMESTAMP NULL,
  locked_by VARCHAR(64) NULL,
  last_error VARCHAR(255) NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_moderation_outbox_entity (entity_type, entity_id),
  KEY idx_moderation_outbox_due (status, next_run_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS moderation_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(32) NOT NULL,
  entity_id BIGINT NOT NULL,
  actor_type VARCHAR(32) NOT NULL,
  actor VARCHAR(64) NOT NULL,
  old_status VARCHAR(32) NULL,
  new_status VARCHAR(32) NOT NULL,
  reason VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_moderation_audit_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS moderation_cases (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(32) NOT NULL,
  entity_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_moderation_cases_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
