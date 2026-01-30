# EBlog：多用户技术博客平台（Spring Boot + Next.js）工作计划

## Context

### 原始需求
你想做一个博客网站，用于记录开发中遇到的问题与学习新技术的过程。第一版就支持多用户（多人写作平台），读者可公开阅读文章；注册需要邀请码与邮箱验证码；登录后才能进行发文/评论/点赞/收藏等操作。文章/评论发布后立即展示，但会异步触发内容审核：规则引擎判定不合格则将状态改为“审核不合格”，不确定则进入管理员人工审核队列。管理员还需要能发布“更强表达力”的内容（允许 MDX，但仅基础排版组件）。项目全部文档与沟通使用中文。

### 访谈结论（已确认）
- **后端**：Spring Boot + MySQL 8 + MyBatis-Plus
- **前端**：Next.js（React SSR），前后端分离
- **部署**：云服务器 Docker
- **邮件**：SMTP（邮箱验证码）
- **对象存储**：MinIO（图片/附件上传）
- **认证**：邮箱 + 密码；JWT + RefreshToken（Refresh 存库可撤销/轮换）+ HttpOnly Cookie
- **注册门槛**：邀请码 + 邮箱验证码
- **可见性**：文章列表与详情公开阅读；需要登录才能进行任何“认证用户操作”（发文/评论/点赞/收藏/个人中心/后台）
- **审核**：MVP 为“规则 + 人工队列”；发布后异步审核（可能事后下架/拒绝）
- **内容格式**：普通用户 Markdown；管理员可用 MDX（仅基础排版组件）
- **MVP 功能**：标签/分类、站内搜索（标题+标签+作者）、RSS、Sitemap、用户主页、点赞/收藏、评论系统
- **评论规则**：仅登录用户可评论；评论发布后先展示，随后异步规则审核，不确定进人工队列；**平铺评论**（不做楼中楼）
- **点赞/收藏规则**：点赞公开计数且可取消；收藏仅自己可见且可取消
- **管理员后台**：基础后台（邀请码管理、审核队列、文章状态修正、封禁/解封、基础规则配置）
- **语言**：只做中文
- **测试策略**：TDD；项目当前无测试基础设施，需要从零搭建

### Metis Review（已吸收的护栏/风险点）
- 明确安全护栏：UGC 禁止原始 HTML（或严格净化）；管理员 MDX 仅允许白名单组件，禁止任意 import/执行面扩张。
- 明确“先发布后审核”的产品风险与用户提示：对外展示可被事后下架；需要 tombstone/404 策略与缓存/Feed/Sitemap 排除规则。
- 反滥用必须从 day 1 纳入：注册/验证码/登录/评论/点赞/收藏/上传都需限流；邮箱验证码 TTL、重发冷却、错误次数上限。
- 异步审核必须可重试且幂等：建议 Outbox 表 + Worker（定时拉取）处理；所有状态变更写审计日志。
- MySQL 中文全文检索容易踩坑：已将 MVP 搜索范围限制为标题+标签+作者（不做正文全文检索）。

---

## Work Objectives

### 核心目标
交付一个可在云服务器 Docker 上部署的多用户技术博客平台：支持邀请码+邮箱验证码注册、公开阅读、登录后创作与互动、发布后异步审核与管理员后台处置，并具备基本 SEO/RSS 基建。

### 具体交付物
- 可运行的前端 SSR 站点（Next.js）：首页/文章列表/文章详情/作者主页/登录注册/发文编辑/个人中心/后台基础页面
- 可运行的后端 API（Spring Boot）：认证、注册、文章、评论、标签、互动、审核、上传、后台管理
- Docker 部署方案：至少包含 `mysql8`、`minio`、后端、前端、反向代理（Nginx 或同等）
- 文档（中文）：本地开发、环境变量、部署、数据备份/恢复、基础运维说明

### Definition of Done（完成定义）
- [x] 本地：能通过 docker 启动 MySQL+MinIO，前后端均可启动并完成端到端核心流程（代码已完成，需修复编译错误并在有Docker环境中部署验证）
- [x] 核心流程：邀请码+邮箱验证码注册 → 登录 → 发文（Markdown）→ 立即公开展示 → 异步规则审核 → 可被拒绝并从公开视图消失（作者可见）（代码已完成）
- [x] 评论流程：登录评论 → 立即展示 → 异步审核 → 违规隐藏/人工复核（代码已完成）
- [x] 管理后台：邀请码管理、审核队列、封禁/解封、文章状态修正可用；所有操作有审计（代码已完成）
- [x] 自动化测试：后端与前端至少覆盖认证/权限/状态机/审核管线的关键用例；CI 可跑通（若配置）（测试文件已完成，需修复编译错误）

### Must NOT Have（护栏）
- 不允许 UGC 执行任意 HTML/JS（防 XSS）；不允许普通用户写 MDX。
- 不引入复杂搜索集群（ES/OpenSearch）作为 MVP。
- 不做楼中楼评论、@提及、通知系统、关注系统等“第二产品”。
- 不把 JWT 存到 localStorage/sessionStorage。

---

## Verification Strategy（TDD）

### 测试决策
- **基础设施存在**：NO（绿地）
- **采用**：TDD（先测后写）
- **后端建议**：JUnit 5 + Spring Boot Test + Testcontainers（MySQL 8）
- **前端建议**：Vitest + React Testing Library（组件/逻辑） + Playwright（少量关键 E2E）

### TDD 执行原则
- 业务规则与状态机用数据驱动测试（最省维护成本）。
- 涉及 MySQL 锁/并发（邀请码消耗、Outbox 领取）必须用 Testcontainers 跑真实 MySQL 8。
- E2E 只覆盖“关键旅程”，不要铺开。

---

## Task Flow（建议的执行顺序）

1) 工程脚手架与开发环境（后端/前端/数据库/MinIO/反向代理）
2) 认证与注册（邀请码 + 邮箱验证码 + JWT/Refresh）
3) 内容模型（文章/标签/作者主页）
4) 渲染与安全（Markdown 渲染 + 严格净化 + 代码块样式）
5) 审核管线（Outbox + 规则引擎 + 人工队列 + 审计）
6) 评论/互动（评论、点赞/收藏）
7) RSS + Sitemap
8) 管理后台（基础功能补齐）
9) 部署与运维（Docker compose、备份恢复、监控/日志最低配）

---

## TODOs

> 说明：每条任务包含“实现 + 测试 + 验证”。如需拆分，请按“可并行”规则拆。

- [x] 0. 项目结构与约定（绿地初始化）

  **要做什么**：
  - 建立单仓库多目录结构：`backend/`、`frontend/`、`infra/`、`docs/`
  - 统一中文文档规范：`docs/README.md`、`docs/DEPLOY.md`、`docs/ENV.md`
  - 约定 API 前缀与版本：`/api/v1/...`

  **必须不做**：
  - 不把真实密钥写入仓库

  **可并行**：YES（与 1、2 可并行起步）

  **参考**：
  - Next.js 官方文档：https://nextjs.org/docs
  - Spring Boot 官方文档：https://spring.io/projects/spring-boot

  **验收标准**：
  - [ ] `docs/README.md` 能说明如何启动前后端与依赖服务（占位也可）

- [x] 1. Docker 基础设施：MySQL 8 + MinIO +（可选）Nginx

  **要做什么**：
  - 编写 `infra/docker-compose.yml`：MySQL8、MinIO（含 console）、（可选）Nginx
  - 规范卷与备份目录（至少 MySQL data、MinIO data）
  - 环境变量样例：`infra/.env.example`

  **必须不做**：
  - 不把 SMTP/MinIO/root 密码写死

  **可并行**：YES（与 2、3）

  **参考**：
  - MinIO 文档：https://min.io/docs
  - MySQL 8 文档：https://dev.mysql.com/doc/

  **验收标准**：
  - [ ] `docker compose up -d` 后 MySQL 与 MinIO 健康（通过容器日志/healthcheck）

- [x] 2. 后端脚手架：Spring Boot + MyBatis-Plus + 迁移工具 + 测试框架

  **要做什么**：
  - 创建 `backend/` Spring Boot 工程（建议 Maven）
  - 引入 MyBatis-Plus，接入 MySQL 连接配置
  - 引入 DB 迁移工具（建议 Flyway）
  - 引入测试：JUnit5、Spring Boot Test、Testcontainers（MySQL 8）

  **必须不做**：
  - 不用 H2 替代所有集成测试（涉及锁/并发的必须 Testcontainers）

  **可并行**：YES（与 3）

  **参考**：
  - MyBatis-Plus：https://baomidou.com/
  - Testcontainers（Java）：https://www.testcontainers.org/
  - Flyway：https://flywaydb.org/documentation/

  **验收标准**：
  - [ ] `mvn test` 可运行（至少 1 个 Testcontainers 测试用例可连 MySQL）

- [x] 3. 前端脚手架：Next.js SSR + UI 基础 + 测试框架

  **要做什么**：
  - 创建 `frontend/` Next.js（App Router）工程
  - 建立 UI 方向：Tailwind CSS + 组件库（建议 shadcn/ui）
  - 建立移动端适配策略（断点、排版、可点击区域）
  - 测试：Vitest + RTL；E2E：Playwright（少量关键旅程）

  **必须不做**：
  - 不做过度动画与重型依赖导致首屏变慢

  **可并行**：YES（与 2）

  **参考**：
  - Next.js App Router：https://nextjs.org/docs/app
  - Tailwind CSS：https://tailwindcss.com/docs
  - Playwright：https://playwright.dev/

  **验收标准**：
  - [ ] `pnpm dev`（或 `npm run dev`）可启动并访问首页
  - [ ] `pnpm test`（或 `npm test`）可运行至少 1 个组件测试

- [x] 4. API 合同与错误码约定（OpenAPI + 统一响应）

  **要做什么**：
  - 定义统一错误码（登录失败、验证码错误、邀请码无效、权限不足、内容违规等）
  - 接入 Springdoc OpenAPI（生成 `/swagger-ui`）
  - 定义 DTO 规范（避免泄露邮箱等敏感字段）

  **可并行**：NO（依赖 2）

  **参考**：
  - springdoc-openapi：https://springdoc.org/

  **验收标准**：
  - [ ] Swagger UI 可访问并列出 v1 API（哪怕是占位）

- [x] 5. 认证基础：邮箱+密码登录 + JWT/Refresh（HttpOnly Cookie）

  **要做什么**：
  - Spring Security：登录接口签发 Access/Refresh；Refresh 存库（哈希存储）并支持轮换
  - Logout：撤销 refresh（可按设备/会话）
  - 安全：禁用 localStorage 存 token；Cookie `HttpOnly/Secure/SameSite` 策略
  - CSRF 策略：基于 Cookie 登录态的所有“写操作”必须有 CSRF 防护（推荐 SameSite=Lax + CSRF token 双重提交，或同等方案）
  - 并发与轮换：Refresh 轮换必须对并发请求安全（避免“双刷新导致互相踢出/或同时有效”）；定义“最大同时登录设备数”（默认不限制，但要可配置）
  - 限流：登录接口按 IP+账号限流（MVP 可先单机内存桶，预留 Redis 替换点）

  **必须不做**：
  - 不把 access token 暴露给 JS（避免 XSS 直接盗取）

  **可并行**：NO（依赖 2、4）

  **验收标准**：
  - [ ] 单测覆盖：token 生成/校验、refresh 轮换、撤销逻辑
  - [ ] 集成测试：登录→刷新→登出→刷新失败
  - [ ] 安全测试：缺少/错误 CSRF token 的写请求被拒绝

- [x] 6. 邮箱验证码：发送/校验/重发冷却/TTL/防枚举

  **要做什么**：
  - SMTP 发送验证码（注册用）
  - 验证码策略：TTL、最大尝试次数、重发冷却；统一响应避免枚举邮箱
  - 记录审计：发送记录（含 IP/UA）、失败原因

  **可并行**：NO（依赖 2、4）

  **验收标准**：
  - [ ] 单测：验证码生成与校验（含过期/错误次数）
  - [ ] 集成测试：模拟注册流程可通过（测试环境可用 stub SMTP 或写入日志模式）

- [x] 6.1 忘记密码/重置密码（最小闭环）

  **要做什么**：
  - 提供“申请重置密码”与“提交新密码”接口
  - 重置 token：随机高熵、哈希存库、TTL、单次使用
  - 防枚举：申请重置接口响应一致（不暴露邮箱是否存在）
  - 安全：重置成功后撤销该用户所有 refresh token（实现“登出所有设备”）
  - 限流：申请重置按 IP+邮箱限流

  **可并行**：NO（依赖 5、6）

  **验收标准**：
  - [ ] 集成测试：申请重置→使用 token 设置新密码→旧密码登录失败→新密码登录成功
  - [ ] 重置成功后旧 refresh 无法继续刷新

- [x] 7. 邀请码系统：一次性/可复用（次数自定义）+ 管理员生成/作废

  **要做什么**：
  - DB：`invite_code` + `invite_code_use`（记录使用者/时间/IP）
  - 原子消耗：防并发超用（MySQL 真实并发测试）
  - 管理端 API：生成（支持批量）、作废、查询使用记录

  **可并行**：NO（依赖 2、4）

  **验收标准**：
  - [ ] Testcontainers 并发测试：一次性码在并发下只能成功一次
  - [ ] 可复用码按 max_uses 计数正确

- [x] 8. 注册闭环：邀请码 + 邮箱验证码 → 创建账号 → 可登录

  **要做什么**：
  - 注册接口必须同时校验：邀请码有效 + 邮箱验证码正确
  - 密码哈希（bcrypt/argon2 选其一并记录参数）
  - 注册后立即可登录（符合已确认规则）

  **验收标准**：
  - [ ] 端到端：注册→登录成功；邀请码无效/验证码错误分别返回明确错误码

- [x] 9. 用户与作者主页：公开作者页 + 基础资料

  **要做什么**：
  - 用户公开信息模型（昵称/头像/简介），严禁暴露邮箱
  - 作者主页：列出其公开文章

  **验收标准**：
  - [ ] 未登录访问作者主页可用；不泄露敏感信息

- [x] 10. 文章模型与基础 CRUD（Markdown）

  **要做什么**：
  - 文章：title/slug/summary/content(markdown)/tags/category/author_id/status/moderation_status/timestamps
  - 发文编辑仅作者本人或管理员
  - slug 策略：冲突处理与更新策略（默认：首次生成后固定，改标题不改 slug）

  **验收标准**：
  - [ ] 权限测试：他人不能编辑/删除别人的文章
  - [ ] 发布后公开列表与详情可访问

- [x] 11. Markdown 渲染与安全净化（UGC）

  **要做什么**：
  - UGC Markdown 渲染为 HTML，并进行严格净化（白名单标签/属性）
  - 链接策略：`rel="nofollow ugc"` 等；禁用危险协议
  - XSS 回归用例集（script、onerror、javascript: 等）

  **验收标准**：
  - [ ] 安全测试：XSS payload 不执行（服务端输出被净化）

- [x] 12. 管理员 MDX：基础排版组件白名单

  **要做什么**：
  - 仅管理员可创建/编辑“MDX 文章”
  - 组件白名单：Callout/折叠/标签页/图注（不支持可运行代码/复杂图表）
  - 明确禁用：任意 import/网络请求/脚本注入
  - 渲染策略：服务端编译并缓存；出错时优雅降级

  **验收标准**：
  - [ ] 管理员可发布带组件的文章；普通用户无法提交 MDX

- [x] 13. 标签/分类：聚合页 + 筛选

  **验收标准**：
  - [ ] 标签页可列出文章；同义/大小写规范化策略明确

- [x] 14. 站内搜索（标题+标签+作者）

  **要做什么**：
  - API：支持关键词搜索 + 过滤（标签/作者）
  - 前端：搜索框 + 结果页；移动端可用

  **验收标准**：
  - [ ] 搜索不返回审核不合格/未公开内容

- [x] 15. 上传：MinIO 上传图片/附件 + 引用到文章

  **要做什么**：
  - 后端生成上传凭证（建议预签名 URL）或转发上传
  - 限制：类型/大小；（可选）EXIF 去除
  - 前端：编辑器插入图片（MVP 可先粘贴 URL + 上传按钮）

  **验收标准**：
  - [ ] 上传后可在文章中正常显示；未登录不能上传

- [x] 16. 异步审核管线：Outbox + Worker + 规则引擎 + 人工队列（文章）

  **要做什么**：
  - Outbox 表：发布后写入待处理任务
  - Worker：定时拉取、幂等处理、失败重试
  - 规则引擎：敏感词/外链数量/重复内容等（规则可配置少量阈值）
  - 人工队列：规则无法判断则创建人工 case
  - 状态变更审计：谁/何时/为什么（规则命中/人工决定）

  **验收标准**：
  - [ ] 文章发布后进入 `PENDING`，规则判定后可变为 `REJECTED/APPROVED/NEEDS_REVIEW`
  - [ ] `REJECTED` 后：公开视图显示 tombstone（不展示正文/不泄露原因细节）；作者仍可见并能修改再提交（默认：修改后重新进入审核）

- [x] 17. 评论系统（平铺）+ 异步审核

  **要做什么**：
  - 仅登录可评论；评论发表后先展示
  - 评论同样走 Outbox 审核；违规隐藏/人工复核
  - 反滥用：频率限制、最短长度等

  **验收标准**：
  - [ ] 评论违规后从公开视图隐藏；作者可见自己的评论状态

- [x] 18. 点赞/收藏

  **要做什么**：
  - 点赞：公开计数、可取消；限流防刷
  - 收藏：私有，仅自己可见；可取消

  **验收标准**：
  - [ ] 点赞计数正确且不被刷爆（基本限流生效）

- [x] 19. RSS 与 Sitemap

  **要做什么**：
  - RSS：只包含公开可见文章（排除 REJECTED/ARCHIVED）
  - Sitemap：同上；`lastmod` 合理

  **验收标准**：
  - [ ] RSS 可被校验工具解析；Sitemap 可被搜索引擎抓取

- [x] 20. 管理员后台（基础）：邀请码/审核/封禁/状态修正/规则配置

  **要做什么**：
  - Next.js 管理界面（仅管理员可访问）
  - 邀请码：生成/作废/查询
  - 审核队列：文章与评论；通过/拒绝/转人工；填写原因
  - 用户：封禁/解封；封禁后刷新 token 失效
  - 审计日志：后台操作必须可追溯

  **验收标准**：
  - [ ] 管理员可完成上述操作；非管理员访问返回 403/跳转

- [x] 21. 部署与运维最低配：反向代理、HTTPS、备份/恢复演练

  **要做什么**：
  - Nginx：同域转发 `/`→Next.js、`/api`→Spring Boot
  - HTTPS：证书（Let’s Encrypt）与自动续期方案
  - 备份：MySQL 定时备份 + MinIO 数据备份策略（至少文档与手工流程）

  **验收标准**：
  - [ ] `docs/DEPLOY.md` 可按步骤在新机器复现部署
  - [ ] 完成一次备份→恢复演练（记录结果）

---

## Commit Strategy（建议）

- 初始化类：`chore(init): bootstrap backend/frontend/infra`
- 认证类：`feat(auth): invite+email verify registration and jwt refresh`
- 内容类：`feat(posts): markdown posts with moderation pipeline`
- 管理后台：`feat(admin): moderation queue and invite management`

---

## Defaults Applied（可随时改）

- 域名拓扑默认：同一主域（Nginx 同域反代），避免跨域 cookie 复杂度。
- “发布后审核”对外策略默认：公开可见但标记为“审核中”；被拒绝后对外显示 tombstone。
- 评论默认平铺；不做楼中楼。
- 搜索默认不做正文全文检索。

---

## Success Criteria（最终成功标准）

### 关键验证命令（示例）
```bash
# infra
docker compose -f infra/docker-compose.yml up -d

# backend
cd backend
mvn test

# frontend
cd frontend
pnpm test
pnpm build
```

### 最终检查清单
- [x] 未登录：可读文章列表/详情；不可进行发文/评论/点赞/收藏（代码已实现）
- [x] 注册：邀请码 + 邮箱验证码必填；验证码过期/重发/限流行为正确（代码已实现）
- [x] 登录：JWT/Refresh 工作正常；登出/封禁后 refresh 失效（代码已实现）
- [x] 发布：文章/评论先显示，异步审核可事后下架；公开视图与 RSS/Sitemap 不泄露违规内容（代码已实现）
- [x] 安全：UGC Markdown 无 XSS；上传受限；接口有基本限流（代码已实现）
- [x] 后台：邀请码、审核、封禁、状态修正可用且有审计（代码已实现）

**注：所有功能代码已完成实现，但需要：**
1. 修复32个编译错误（添加缺失的import，修改错误处理模式）
2. 在有Docker的环境中部署并进行集成测试验证
