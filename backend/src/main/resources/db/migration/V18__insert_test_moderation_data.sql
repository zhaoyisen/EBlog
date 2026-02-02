-- 插入测试审核数据
-- 这些数据用于测试内容审核功能

-- 1. 创建测试用户（如果不存在）
INSERT INTO users (email, password_hash, nickname, role)
VALUES ('test.user@example.com', '$2a$10$test_password_hash', '测试用户', 'USER')
ON DUPLICATE KEY UPDATE id=id;

SET @user_id = LAST_INSERT_ID();

-- 2. 创建需要人工审核的文章（超过10个外链）
-- 根据 RuleEngine，外链超过 max-external-links (默认10) 会触发 NEEDS_REVIEW
INSERT INTO posts (author_id, title, slug, summary, content_markdown, tags_csv, category, status, moderation_status, created_at, updated_at)
VALUES (
  @user_id,
  '测试文章 - 包含大量外链',
  'test-post-with-many-links',
  '这是一篇包含大量外部链接的测试文章，用于触发人工审核流程',
  '# 测试文章

这是一篇测试文章，包含超过10个外部链接。

参考链接：
1. https://example1.com
2. https://example2.com
3. https://example3.com
4. https://example4.com
5. https://example5.com
6. https://example6.com
7. https://example7.com
8. https://example8.com
9. https://example9.com
10. https://example10.com
11. https://example11.com

这篇文章应该会触发 NEEDS_REVIEW 状态，因为外链数量超过了阈值（10个）。',
  '测试,审核',
  '技术',
  'PUBLISHED',
  'NEEDS_REVIEW',
  NOW(),
  NOW()
);

SET @post_id_1 = LAST_INSERT_ID();

-- 3. 创建另一篇需要审核的文章（不同类型）
INSERT INTO posts (author_id, title, slug, summary, content_markdown, tags_csv, category, status, moderation_status, created_at, updated_at)
VALUES (
  @user_id,
  '另一篇测试文章',
  'another-test-post',
  '第二篇测试文章',
  '# 另一篇测试文章

更多测试链接：
- https://test1.com
- https://test2.com
- https://test3.com
- https://test4.com
- https://test5.com
- https://test6.com
- https://test7.com
- https://test8.com
- https://test9.com
- https://test10.com
- https://test11.com
- https://test12.com',
  '测试',
  '技术',
  'PUBLISHED',
  'NEEDS_REVIEW',
  NOW(),
  NOW()
);

SET @post_id_2 = LAST_INSERT_ID();

-- 4. 创建需要审核的评论（超过10个外链）
-- 注意：需要先确保 posts 表中有已发布的文章
-- 这里使用上面刚创建的 @post_id_1
INSERT INTO comments (post_id, author_id, content, status, moderation_status, created_at, updated_at)
VALUES (
  @post_id_1,
  @user_id,
  '这条评论包含很多链接：https://link1.com https://link2.com https://link3.com https://link4.com https://link5.com https://link6.com https://link7.com https://link8.com https://link9.com https://link10.com https://link11.com https://link12.com',
  'PUBLISHED',
  'NEEDS_REVIEW',
  NOW(),
  NOW()
);

SET @comment_id_1 = LAST_INSERT_ID();

-- 5. 创建一条自动审核通过的评论（作为对比）
INSERT INTO comments (post_id, author_id, content, status, moderation_status, created_at, updated_at)
VALUES (
  @post_id_1,
  @user_id,
  '这是一条正常的评论，不应该触发审核。https://example.com',
  'PUBLISHED',
  'APPROVED',
  NOW(),
  NOW()
);

-- 6. 创建一条被拒绝的评论（包含敏感词）
INSERT INTO comments (post_id, author_id, content, status, moderation_status, created_at, updated_at)
VALUES (
  @post_id_1,
  @user_id,
  '这条评论包含spam内容，应该被自动拒绝。',
  'PUBLISHED',
  'REJECTED',
  NOW(),
  NOW()
);

-- 7. 插入一些审核日志（用于测试日志查询功能）
INSERT INTO audit_logs (entity_type, entity_id, actor_id, action, reason, rule_hit, created_at)
VALUES
  ('POST', @post_id_1, @user_id, 'REQUEST_REVIEW', 'Too many external links', 'LINK_COUNT_THRESHOLD', NOW()),
  ('POST', @post_id_2, @user_id, 'REQUEST_REVIEW', 'Too many external links', 'LINK_COUNT_THRESHOLD', NOW()),
  ('COMMENT', @comment_id_1, @user_id, 'REQUEST_REVIEW', 'Too many external links', 'LINK_COUNT_THRESHOLD', NOW());
