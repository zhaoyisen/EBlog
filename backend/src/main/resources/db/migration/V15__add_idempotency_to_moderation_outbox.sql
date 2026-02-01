-- V15__add_idempotency_to_moderation_outbox.sql
-- 为 moderation_outbox 表增加幂等性保护字段与唯一索引

-- 1. 新增幂等键字段
-- deduplication_key: 用于防止同一实体重复处理。
-- 默认值为 'auto' 以保证向后兼容，现有数据将自动填充。
-- 策略：后续逻辑可根据 entity_type + entity_id + 内容哈希生成，或简单使用 entity_type + entity_id。
ALTER TABLE moderation_outbox 
    ADD COLUMN deduplication_key VARCHAR(255) NOT NULL DEFAULT 'auto' COMMENT '幂等键，用于防止同一实体重复处理',
    ADD COLUMN deduplication_key_updated_at TIMESTAMP NULL COMMENT '幂等键更新时间，便于追踪幂等键变化';

-- 2. 更新唯一索引
-- 原索引 uk_moderation_outbox_entity 仅包含 (entity_type, entity_id)。
-- 新索引包含 deduplication_key，允许同一实体在不同幂等键下（如内容更新后）重新入队，
-- 同时保证同一 (entity, key) 组合不会重复处理。
ALTER TABLE moderation_outbox DROP INDEX uk_moderation_outbox_entity;
ALTER TABLE moderation_outbox ADD UNIQUE KEY uk_moderation_outbox_entity (entity_type, entity_id, deduplication_key);
