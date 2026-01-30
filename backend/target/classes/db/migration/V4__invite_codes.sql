CREATE TABLE IF NOT EXISTS invite_codes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  max_uses INT NOT NULL DEFAULT 1,
  used_count INT NOT NULL DEFAULT 0,
  expires_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  revoked_at TIMESTAMP NULL,
  UNIQUE KEY uk_invite_codes_code (code),
  KEY idx_invite_codes_status (status),
  KEY idx_invite_codes_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS invite_code_uses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  invite_code_id BIGINT NOT NULL,
  used_by_user_id BIGINT NULL,
  used_ip VARCHAR(64) NULL,
  used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_invite_code_uses_code_id (invite_code_id),
  KEY idx_invite_code_uses_used_by (used_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
