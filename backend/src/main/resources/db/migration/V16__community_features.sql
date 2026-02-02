-- Create user_follows table
CREATE TABLE IF NOT EXISTS user_follows (
    follower_id BIGINT NOT NULL COMMENT '关注者ID',
    followee_id BIGINT NOT NULL COMMENT '被关注者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id),
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users(id),
    INDEX idx_followee_id (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- Create announcements table
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- Alter posts table to add community features
ALTER TABLE posts
    ADD COLUMN view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量' AFTER moderation_status,
    ADD COLUMN is_pinned BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否置顶' AFTER view_count,
    ADD COLUMN is_featured BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否精选' AFTER is_pinned;

-- Add FULLTEXT index for better search capabilities
-- Using title, summary, and content_markdown as requested
ALTER TABLE posts ADD FULLTEXT idx_posts_fulltext (title, summary, content_markdown);
