# Draft: EBlog 页面重新设计

## Original Request
用户："重新设计整个项目的页面，让其更像一个社区博客，比如现在的首页感觉没啥用，可以参考github和linux.do等极客风格的博客网站。"

## Requirements (confirmed)
- **设计范围**：全部页面（重新设计整个前台页面系统，不含后台管理）
- **设计风格**：混合风格（首页GitHub风格清爽，列表/详情页Linux.do风格密集）
- **首页内容**：热门/精选文章 + 分类导航 + 社区公告/置顶 + 活跃作者
- **主题切换**：实现亮色/暗色切换功能
- **信息密度**：增加文章卡片的元数据（评论数、浏览量、点赞数）

## Research Findings

### 当前页面结构（来自 bg_821cf18b）
**核心页面：**
- `/` - 首页：静态介绍页面，Hero区域 + 功能特性 + CTA按钮
- `/posts` - 文章列表：网格布局，展示PostCard（20条），支持分页
- `/posts/[slug]` - 文章详情：MarkdownRenderer + PostInteractions（点赞/收藏/评论）
- `/profile` - 个人中心：三标签页（编辑资料/我的文章/账户设置）

**辅助页面：**
- `/tags`, `/tags/[tag]` - 标签列表/详情
- `/categories` - 分类列表
- `/search` - 搜索页
- `/authors`, `/authors/[id]` - 作者列表/详情
- `/upload`, `/login`, `/register` - 功能页面

**管理后台：**
- `/admin/*` - 需ADMIN角色：用户管理、内容审核、邀请码管理

**现有组件：**
- `Navbar` - 响应式导航栏（桌面/移动端不同布局）
- `PostCard` - 文章卡片（标题、摘要、分类、标签、作者、日期、hover效果）
- `MarkdownRenderer` - Markdown渲染器（react-markdown + highlight.js）
- `ByteMDEditor` - Markdown编辑器（bytemd/react，中文界面）

**样式系统：**
- Tailwind CSS + CSS变量主题系统
- 亮色/暗色主题（仅定义变量，未实现切换功能）
- 圆角：0.75rem（12px）
- 字体：Noto Sans SC（正文）+ JetBrains Mono（代码）
- 颜色：中性色系（neutral-50 到 neutral-900）

### 参考网站设计研究（来自 bg_fbefb6a3）

**6个核心设计原则：**

1. **层级驱动的视觉系统**（GitHub）
   - 清晰的视觉层次引导用户注意力
   - Hero区域：大标题 + 简短描述 + 主次分明的CTA按钮
   - 使用tracking-tight字距，大字号对比

2. **卡片式内容组织**（Discourse + GitHub）
   - 内容封装在卡片容器中（圆角8-12px，轻微阴影）
   - hover时上浮效果（translateY(-2px) + 阴影增强）
   - 清晰的边框和分离感

3. **徽章与标签系统**（GitHub + Discourse）
   - 彩色徽章传达元数据（分类、状态、等级）
   - GitHub：bug(红)、enhancement(蓝)、documentation(紫)
   - Discourse：Solved、Pinned、Category标签

4. **暗色模式的智能适配**（2026标准实践）
   - 使用深灰而非纯黑（GitHub: #0d1117）
   - 文字对比度 ≥ 4.5:1
   - 代码块语法高亮自动适配主题

5. **紧凑但可读的信息密度**（Discourse）
   - 有限空间内展示更多信息
   - 小字号（text-xs/sm）+ 紧凑间距
   - 丰富元数据：作者、日期、评论数、浏览量、点赞数

6. **微交互与反馈**（GitHub）
   - 每个交互都有视觉反馈（hover、active、loading）
   - 过渡动画（0.15s ease）
   - 按钮active状态scale(0.98)
   - loading状态使用skeleton或spinner

**具体UI模式：**
- 顶部导航栏（GitHub风格）：Logo + 主导航 + 搜索 + 主题切换 + 用户操作
- 文章卡片（融合风格）：元数据行 + 标题 + 摘要 + 作者 + 互动数据（评论/点赞/浏览）
- 标签页导航（GitHub Issues风格）：border-b-2指示器 + 计数徽章
- 评论列表（Discourse风格）：Avatar + 作者信息 + Markdown内容 + 互动按钮

## Technical Decisions
- **保留现有架构**：保留Next.js 15 + Tailwind CSS基础架构
- **优化样式系统**：优化暗色模式配色（使用深灰#0d1117，非纯黑），实现主题切换功能
- **组件复用**：保留现有PostCard基础设计，增强元数据展示
- **响应式设计**：保持移动端优先策略，优化移动端体验
- **徽章系统**：实现分类彩色徽章、状态徽章（置顶、精选）

## Open Questions
- [ ] 后端支持确认（CRITICAL）：后端API是否需要补充以下接口？
  - 搜索功能（POST /api/v1/posts/search 或 GET /api/v1/posts?q=xxx）
  - 用户关注（user_follows表 + follow/unfollow/getFollowing接口）
  - 用户统计（post_count, comment_count, total_views, total_likes）
  - 活动时间线（user_activity表或基于现有数据聚合）
  - 热门文章（按热度排序接口）
  - 活跃作者（按最近活动排序接口）
  - 公告系统（announcements表 + 接口）
  - 文章浏览量（posts表添加view_count字段 + 计数接口）
- [ ] 实施策略：是否分阶段实施？（Phase 1：UI重新设计，Phase 2：新功能）

## Scope Boundaries
- **INCLUDE**：
  - 所有前台页面重新设计（首页、文章列表、文章详情、分类、标签、作者、搜索、个人中心、登录、注册）
  - 主题切换功能（亮色/暗色）
  - 增强文章卡片（评论数、浏览量、点赞数）
  - 徽章系统（分类彩色徽章、状态徽章）
  - 微交互（hover、active、loading状态）
  - 暗色模式优化（配色、对比度、代码高亮）
- **EXCLUDE**：
  - 后台管理页面（/admin/*）
  - 自动化测试（用户选择手动验证）
  - 复杂动画（页面加载交错动画、滚动触发动画 - 作为可选优化）

**注意**：
  - 搜索功能、用户关注功能需要后端API支持，计划中将包含后端接口需求文档
  - 个人中心增强（用户统计、活动时间线）需要后端提供相关数据接口

## Test Strategy Decision
- **User wants tests**: NO - 手动验证
- **Verification method**: 手动验证（浏览器访问、截图对比）
- **Test commands**: `npm -C frontend run dev` - 启动开发服务器手动验证
- **Evidence required**: 浏览器截图（每次重大变更后）

## 新增需求（用户确认）
1. **搜索功能**：实现实际的搜索逻辑（全文搜索、分类搜索、标签搜索）
2. **登录/注册页**：重新设计，与整体风格统一
3. **个人中心增强**：添加用户统计、活动时间线等
4. **用户关注功能**：实现关注用户、查看关注列表、获取关注动态

## Backend API Audit Results（关键发现）

### 后端现有能力：
✅ **已有表**：
- `posts`（V8）：包含id, author_id, title, slug, summary, content_markdown, tags_csv, category, status, moderation_status, created_at, updated_at
- `post_likes`（V13）：点赞表
- `post_favorites`（V13）：收藏表
- `comments`（V12）：评论表

✅ **已有接口**：
- GET /api/v1/posts - 文章列表（PostSummary包含id, authorId, authorName, authorAvatar, title, slug, summary, tagsCsv, category, createdAt, status）
- GET /api/v1/posts/{slug} - 文章详情（PostDetail包含完整信息）
- POST /api/v1/posts - 创建文章
- PUT /api/v1/posts/{id} - 更新文章
- DELETE /api/v1/posts/{id} - 删除文章

### 后端缺失能力（需要补充）：
❌ **缺失字段**：
- `posts` 表没有 `view_count` 字段（无法显示浏览量）
- `posts` 表没有 `is_pinned` 字段（无法显示置顶）
- `posts` 表没有 `is_featured` 字段（无法显示精选）
- `PostSummary` DTO 没有返回 `viewCount`, `likeCount`, `commentCount`

❌ **缺失表**：
- `user_follows` 表（用户关注功能）
- `announcements` 表（公告系统）
- `user_activity` 表（活动时间线，可基于现有数据聚合）

❌ **缺失接口**：
- 搜索接口（/api/v1/posts/search 或 /api/v1/posts?q=xxx）
- 用户关注接口（POST/DELETE /api/v1/users/:id/follow, GET /api/v1/me/following）
- 用户统计接口（GET /api/v1/users/:id/stats）
- 活跃作者接口（GET /api/v1/users/active 或 /api/v1/users?sort=recent_activity）
- 热门文章接口（GET /api/v1/posts?sort=hot&limit=10）
- 公告接口（GET /api/v1/announcements）

### 前端配置缺失：
❌ **Tailwind配置**：
- `tailwind.config.ts` 没有 `darkMode: 'class'` 配置（主题切换功能需要）

### 关键决策点：
1. **搜索功能**：前端过滤 vs 后端搜索（后端未实现）
2. **用户关注**：需要后端实现（表 + 接口）或暂不实现
3. **文章元数据**：需要后端添加字段（view_count）和聚合查询（likeCount, commentCount）
4. **热门/精选文章**：需要后端添加字段（is_pinned, is_featured）和排序接口
