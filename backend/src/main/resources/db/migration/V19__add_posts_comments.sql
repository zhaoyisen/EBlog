-- 为posts表添加字段注释
-- 添加日期: 2026-02-03

ALTER TABLE posts
  MODIFY COLUMN id BIGINT COMMENT '文章ID（主键）';

ALTER TABLE posts
  MODIFY COLUMN author_id BIGINT COMMENT '作者ID（关联users表）';

ALTER TABLE posts
  MODIFY COLUMN format VARCHAR(32) COMMENT '文章格式：MARKDOWN-标准Markdown，MDX-增强Markdown（仅管理员可用）';

ALTER TABLE posts
  MODIFY COLUMN title VARCHAR(255) COMMENT '文章标题';

ALTER TABLE posts
  MODIFY COLUMN slug VARCHAR(255) COMMENT '文章URL标识符（唯一，用于友好URL）';

ALTER TABLE posts
  MODIFY COLUMN summary VARCHAR(1024) COMMENT '文章摘要（用于列表展示）';

ALTER TABLE posts
  MODIFY COLUMN content_markdown MEDIUMTEXT COMMENT '文章正文内容（Markdown格式）';

ALTER TABLE posts
  MODIFY COLUMN tags_csv VARCHAR(1024) COMMENT '文章标签（逗号分隔的字符串，如：Java,Spring,数据库）';

ALTER TABLE posts
  MODIFY COLUMN category VARCHAR(255) COMMENT '文章分类（如：技术、生活、其他）';

ALTER TABLE posts
  MODIFY COLUMN status VARCHAR(32) COMMENT '文章发布状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档';

ALTER TABLE posts
  MODIFY COLUMN moderation_status VARCHAR(32) COMMENT '内容审核状态：PENDING-待审核，APPROVED-审核通过，REJECTED-审核拒绝，NEEDS_REVIEW-需人工审核';

ALTER TABLE posts
  MODIFY COLUMN created_at TIMESTAMP COMMENT '创建时间';

ALTER TABLE posts
  MODIFY COLUMN updated_at TIMESTAMP COMMENT '更新时间';

ALTER TABLE posts
  MODIFY COLUMN view_count INT COMMENT '浏览次数';

ALTER TABLE posts
  MODIFY COLUMN is_pinned BOOLEAN COMMENT '是否置顶：true-置顶，false-不置顶';

ALTER TABLE posts
  MODIFY COLUMN is_featured BOOLEAN COMMENT '是否精选：true-精选，false-非精选';
