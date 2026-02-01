# PROJECT KNOWLEDGE BASE

**Generated:** 2026-02-01
**Commit:** d6dcedd
**Branch:** main

## OVERVIEW
EBlog：面向开发者的多用户技术博客平台。前端 `frontend/` 为 Next.js 15（React 19，App Router），后端 `backend/` 为 Spring Boot 3.2（Java 17，Spring Security，MyBatis-Plus，Flyway），`infra/` 提供 Docker Compose + Nginx 反代。

## STRUCTURE
```
./
├── backend/            # Spring Boot 后端（API、鉴权、审核、上传、数据访问）
├── frontend/           # Next.js 前端（页面、/api 代理、AuthProvider、MDX）
├── infra/              # docker-compose + nginx 反代 + env 模板
└── docs/               # 中文文档与实现计划（docs/plans/*.md）
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 前端页面（App Router） | `frontend/src/app` | 路由即目录；部分页面为 Client Component（`"use client"`） |
| 前端鉴权/请求封装 | `frontend/src/lib/auth/AuthProvider.tsx` | refresh cookie + CSRF + 内存 access token；401 自动 refresh 并重试 |
| Next `/api/*` 代理 | `frontend/src/app/api/[...path]/route.ts` | 代理到后端；重点：多条 `Set-Cookie` 透传 |
| 后端鉴权与安全 | `backend/src/main/java/com/eblog/auth` | JWT + refresh_token（HttpOnly Cookie）；CSRF 规则见 `SecurityConfig` |
| 后端 API 入口与统一错误 | `backend/src/main/java/com/eblog/api/common` | `ApiResponse`/`ErrorCode`/`GlobalExceptionHandler` |
| 数据库迁移 | `backend/src/main/resources/db/migration` | Flyway：`V{N}__*.sql` |
| 反代与部署拓扑 | `infra/nginx/nginx.conf` | `/` -> 前端，`/api/` -> 后端，`/minio/` -> MinIO |
| 配置说明（文档） | `docs/ENV.md` | 注意：文档与 infra 的 env 注入策略可能不一致，改动前先核对 |

## CODE MAP (NO LSP)
- **Backend entry**: `backend/src/main/java/com/eblog/EblogApplication.java`（`@EnableScheduling`）
- **Security**: `backend/src/main/java/com/eblog/auth/SecurityConfig.java`（CSRF + JWT Filter）
- **Auth endpoints**: `backend/src/main/java/com/eblog/auth/AuthController.java`（`/api/v1/auth/*`）
- **Posts endpoints**: `backend/src/main/java/com/eblog/post/PostController.java`（`/api/v1/posts/*`）
- **Me endpoints**: `backend/src/main/java/com/eblog/user/MeController.java`（`/api/v1/me/*`）
- **Next proxy**: `frontend/src/app/api/[...path]/route.ts`
- **Auth provider**: `frontend/src/lib/auth/AuthProvider.tsx`
- **MDX safety**: `frontend/src/lib/mdx/remarkSafeMdx.ts`

## CONVENTIONS
- **Monorepo**：前后端与基础设施同仓；不要把 `frontend/.next/`、`backend/target/` 当作源码阅读入口。
- **API path**：后端 REST 统一在 `/api/v1/*`；前端浏览器侧默认走 `appConfig.apiBase`（当前为 `/api`）。
- **Auth shape**：后端发放 `refresh_token`（HttpOnly + SameSite=Strict）+ 返回 `accessToken`；前端只把 access token 保存在内存并用 refresh 自动续期。
- **CSRF**：后端使用 `CookieCsrfTokenRepository` 下发 `XSRF-TOKEN`；前端从 cookie 取值并在写请求带 `X-XSRF-TOKEN`。

## ANTI-PATTERNS (THIS PROJECT)
- **不要提交真实密钥/密码**：`infra/.env` 明确要求不要提交；同时 `backend/src/main/resources/application.yml` 当前含敏感配置示例（应视为风险源，改动时务必先处理策略）。
- **不要依赖构建时 ESLint**：`frontend/next.config.ts` 设定 `eslint.ignoreDuringBuilds: true`；若要强制 lint，跑 `npm -C frontend run lint`。
- **不要在浏览器落盘 access token**：登录态以 refresh cookie + 内存 access token 为主，`frontend/src/__tests__/login.test.tsx` 约束不写 `localStorage`。

## COMMANDS
```bash
# infra: 启动依赖服务（MySQL/MinIO）
docker compose -f infra/docker-compose.yml up -d

# backend: 测试/运行
mvn -f backend/pom.xml test
mvn -f backend/pom.xml spring-boot:run

# frontend: 开发/测试/构建
npm -C frontend install
npm -C frontend run dev
npm -C frontend run test:ci
npm -C frontend run build
```

## NOTES
- **LSP**：当前环境未安装 `typescript-language-server` 与 `jdtls`，AGENTS 内容以 grep/ast-grep 为主；如需要 LSP，请先安装对应 server。
- **配置策略有冲突迹象**：`docs/ENV.md` 声明“不使用环境变量”，但 `infra/docker-compose*.yml` 与 `infra/.env.example` 仍以 env 形式注入；修改部署/配置前先统一口径。
