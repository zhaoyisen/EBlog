# 文章状态机与可见性规则

本文档明确了 EBlog 平台中文章的 `status`（发布状态）与 `moderationStatus`（审核状态）的定义、转换逻辑以及在不同角色下的可见性规则。

## 1. 状态定义

### 1.1 发布状态 (`status`)
反映作者对文章生命周期的控制。
- **DRAFT (草稿)**: 初始状态。仅作者可见，不进入审核流程。
- **PUBLISHED (已发布)**: 文章对公众可见（取决于审核状态），并触发异步审核流程。
- **ARCHIVED (已归档)**: 文章被作者或管理员下线。公众不可见，保留数据。

### 1.2 审核状态 (`moderationStatus`)
反映平台对内容的合规性控制。采用“**先发后审**”策略。
- **PENDING (待审核)**: 文章发布后的初始审核状态。由于“先发后审”，此状态下文章对公众可见。
- **APPROVED (已通过)**: 审核通过。文章继续公开，并允许进入 RSS/站点地图。
- **REJECTED (已拒绝)**: 审核不通过。文章对公众隐藏，作者需修改后重新发布。
- **NEEDS_REVIEW (需人工复审)**: 自动审核无法确定，等待管理员人工介入。此状态下文章维持公开。

---

## 2. 可见性矩阵

| status | moderationStatus | 游客/其他用户 | 作者本人 | 管理员 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| DRAFT | (任何) | 不可见 | **可见** | 不可见 | 草稿阶段不触发审核 |
| PUBLISHED | PENDING | **可见** | **可见** | **可见** | 先发后审：待审时公开 |
| PUBLISHED | APPROVED | **可见** | **可见** | **可见** | 正常公开状态 |
| PUBLISHED | REJECTED | 不可见 | **可见** | **可见** | 违规隐藏；作者可见理由并修改 |
| PUBLISHED | NEEDS_REVIEW | **可见** | **可见** | **可见** | 复审期间维持公开 |
| ARCHIVED | (任何) | 不可见 | **可见** | **可见** | 归档后仅相关人员可见 |

---

## 3. API 行为规范

### 3.1 公共文章列表 (`GET /api/v1/posts`)
- **过滤条件**: `status = 'PUBLISHED' AND moderationStatus != 'REJECTED'`
- **排序**: `created_at DESC`
- **实现参考**: `PostMapper.listPublic`

### 3.2 公共文章详情 (`GET /api/v1/posts/{slug}`)
- **访问控制**: 
  - 若文章为 `PUBLISHED` 且非 `REJECTED`，允许匿名访问。
  - 若文章为 `DRAFT` 或 `ARCHIVED` 或 `REJECTED`，仅允许作者本人或管理员访问（需带 Token）。
- **实现参考**: `PostController.get` (当前实现对非公开状态直接返回 404，建议后续优化为区分权限)

### 3.3 RSS 与 站点地图 (`/feed`, `/sitemap.xml`)
- **过滤条件**: `status = 'PUBLISHED' AND moderationStatus = 'APPROVED'`
- **理由**: 外部索引与订阅仅包含经过平台背书的高质量/合规内容。
- **实现参考**: `FeedController`

### 3.4 作者个人中心 (`GET /api/v1/me/posts`)
- **过滤条件**: `author_id = {current_user_id}`
- **包含内容**: 所有状态的文章。
- **UI 提示**: 需明确标注“草稿”、“审核中”、“已拒绝（含理由）”等标签。

### 3.5 管理员审核队列 (`GET /api/v1/admin/moderation/review-queue`)
- **过滤条件**: `moderationStatus IN ('PENDING', 'NEEDS_REVIEW')`
- **实现参考**: `AdminModerationController.getReviewQueue`

---

## 4. 推荐默认策略：先发后审

EBlog 默认采用“先发后审”以提升用户发布体验：
1. 作者点击“发布”，`status` 变为 `PUBLISHED`，`moderationStatus` 设为 `PENDING`。
2. 文章立即在首页列表出现，允许通过 URL 访问。
3. 后端 `WorkerService` 异步扫描 `outbox` 表，调用 `RuleEngine` 进行自动审核。
4. 若自动审核通过，状态转为 `APPROVED`；若发现违规，状态转为 `REJECTED` 并从公共列表消失。

---

## 5. 前端 UI 最低要求

- **列表页**: 
  - 正常显示所有公开文章。
  - 作者在自己的列表页看到 `PENDING` 文章时，应有“审核中”提示。
- **详情页**:
  - 若文章处于 `REJECTED` 状态且访问者是作者，页面顶部应显示红色警告条，告知违规原因。
  - 若文章处于 `DRAFT` 状态，显示“预览模式”水印。
- **编辑器**:
  - 提供“保存草稿”与“直接发布”两个按钮。

---

## 6. 后续改造清单

为了完全符合上述规则，建议进行以下代码统一：
1. **统一过滤逻辑**: `PostMapper.listPublic` 与 `FeedController` 的过滤条件目前不一致（前者允许 PENDING，后者仅限 APPROVED），这是符合预期的，但需在代码注释中明确。
2. **详情页权限细化**: `PostController.get` 目前对 `REJECTED` 文章一律返回 404。应修改为：如果是作者本人访问，允许查看并显示审核失败原因。
3. **新增作者文章接口**: 实现 `GET /api/v1/me/posts`，支持按状态筛选，方便作者管理内容。
4. **审核理由下发**: 在 `PostEntity` 或关联表中增加 `moderation_reason` 字段，并在 `REJECTED` 时返回给作者。
5. **搜索索引同步**: 确保搜索接口（`SearchController`）的过滤逻辑与 `listPublic` 一致，避免搜到已拒绝或草稿内容。
