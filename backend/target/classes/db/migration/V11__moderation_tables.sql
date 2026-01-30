-- 异步任务Outbox表
CREATE TABLE IF NOT EXISTS outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型：POST, COMMENT等',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, PROCESSING, COMPLETED, FAILED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    attempts INT DEFAULT 0 COMMENT '重试次数',
    last_error TEXT COMMENT '最后错误信息',
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步任务Outbox表';

-- 审核审计日志表
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型：POST, COMMENT等',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    actor_id BIGINT COMMENT '操作者ID（管理员ID）',
    action VARCHAR(50) NOT NULL COMMENT '操作类型：APPROVE, REJECT, REQUEST_REVIEW, RULE_REJECT',
    reason TEXT COMMENT '操作原因',
    rule_hit VARCHAR(255) COMMENT '命中的规则名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_actor (actor_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核审计日志表';
