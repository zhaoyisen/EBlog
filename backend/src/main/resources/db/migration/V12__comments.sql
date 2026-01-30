CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '文章ID',
    author_id BIGINT NOT NULL COMMENT '评论者ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID（平铺，暂不实现嵌套）',
    content TEXT NOT NULL COMMENT '评论内容',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态：PUBLISHED, HIDDEN',
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING, APPROVED, REJECTED, NEEDS_REVIEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status, moderation_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
