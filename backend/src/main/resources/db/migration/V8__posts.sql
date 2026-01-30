CREATE TABLE IF NOT EXISTS posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL,
  summary VARCHAR(1024) NULL,
  content_markdown MEDIUMTEXT NOT NULL,
  tags_csv VARCHAR(1024) NULL,
  category VARCHAR(255) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  moderation_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_posts_slug (slug),
  KEY idx_posts_author_id (author_id),
  KEY idx_posts_status (status),
  KEY idx_posts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
