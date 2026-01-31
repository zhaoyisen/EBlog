# Agent Instructions (EBlog)

本文件会提供给在本仓库中自动工作的智能编码代理。请严格遵守。

## 语言与沟通

- 全程使用中文与用户交流。
- 代码注释一律中文；文档优先中文。

## 仓库结构

- `frontend/`：Next.js 15 + React 19（App Router，代码在 `frontend/src/app/`）
- `backend/`：Spring Boot 3.2（Java 17）+ MyBatis-Plus + Flyway
- `infra/`：Docker Compose（MySQL + MinIO；生产 compose 还包含 backend/frontend/nginx）
- `docs/`：项目文档（中文）；配置清单见 `docs/ENV.md`

## 安全与仓库卫生（强制）

- 不要提交敏感信息：`.env`、密钥、证书、真实密码等（见根目录 `.gitignore`）。
- 变更最小化：只改必要文件，避免无关重构/大范围格式化。
- 提交前最小验证：能跑单测就跑；不能就至少跑 lint + 构建/编译。

## 构建 / Lint / 测试命令

说明：本仓库是多工程结构；在根目录执行时使用 `npm -C frontend ...` / `mvn -f backend/pom.xml ...`。

### Frontend（Next.js + Vitest）

```bash
# 安装依赖（使用已提交的 lockfile：frontend/package-lock.json）
npm -C frontend install

# 开发/构建/启动
npm -C frontend run dev
npm -C frontend run build
npm -C frontend run start

# Lint（必须 0 warning；next build 不会挡 lint）
npm -C frontend run lint

# 测试（交互/监听）
npm -C frontend run test

# 测试（CI，一次性跑完）
npm -C frontend run test:ci

# 只跑单个测试文件
npm -C frontend run test:ci -- src/__tests__/home.test.tsx

# 只跑单个用例（按名称）
npm -C frontend run test:ci -- -t "renders Chinese title"

# 文件 + 用例
npm -C frontend run test:ci -- src/__tests__/home.test.tsx -t "renders Chinese title"
```

### Backend（Spring Boot + Maven + JUnit）

```bash
# 编译/打包
mvn -f backend/pom.xml -DskipTests package

# 运行
mvn -f backend/pom.xml spring-boot:run

# 跑全部测试
mvn -f backend/pom.xml test

# 只跑单个测试类/方法（Surefire）
mvn -f backend/pom.xml -Dtest=TagParserTest test
mvn -f backend/pom.xml -Dtest=TagParserTest#normalizeLowercasesAndTrims test
```

Testcontainers 集成测试：默认跳过；需要 Docker，并设置系统属性 `-Deblog.testcontainers=true`（示例见 `backend/src/test/java/com/eblog/auth/RegisterIntegrationTest.java`）。

### Infra（Docker Compose）

```bash
# 本地：MySQL + MinIO
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml ps

# 生产 compose（含 backend/frontend/nginx，带构建）
docker compose -f infra/docker-compose.prod.yml up -d --build
```

注意：`infra/docker-compose.yml` 依赖外部环境变量（例如 `MYSQL_ROOT_PASSWORD`）；参考 `docs/ENV.md`。

## 代码风格与约定

### 通用

- 缩进：2 空格（前后端现有代码均如此）。
- 行尾与引号：前端现有代码使用双引号 + 分号；保持一致。
- 注释：只在“非显而易见”的逻辑处加注释；注释用中文。

### TypeScript / React / Next.js（`frontend/`）

- 路由：App Router 页面组件放 `frontend/src/app/**/page.tsx`，布局用 `layout.tsx`。
- 类型：`tsconfig.json` 启用 `strict: true`；避免 `any`，确需使用时局部 `eslint-disable` 并说明原因（例：`frontend/src/lib/mdx/renderMdx.tsx`）。
- 导入：
  - 类型导入使用 `import type { ... } from "..."`。
  - 顺序：Node 内置 -> 三方库 -> `@/` 别名 -> 相对路径。
  - 别名：`@/*` -> `frontend/src/*`（见 `frontend/tsconfig.json`）。
- 命名：组件/类型 `PascalCase`；函数/变量 `camelCase`；hooks 以 `use` 开头；常量用 `UPPER_SNAKE_CASE` 或语义清晰的 `camelCase`（按现有文件风格就近）。
- 测试：Vitest + Testing Library；全局断言来自 `frontend/src/test/setup.ts`。
- 配置文件：前端 API 基地址使用 `frontend/src/config/appConfig.ts`（参见 `docs/ENV.md`）。

### Java / Spring Boot（`backend/`）

- API 返回：统一用 `ApiResponse.ok(...)` / `ApiResponse.fail(...)`（`backend/src/main/java/com/eblog/api/common/ApiResponse.java`）。
- 错误码：统一复用/扩展 `ErrorCode`（`backend/src/main/java/com/eblog/api/common/ErrorCode.java`），避免散落字符串。
- 异常处理：
  - `GlobalExceptionHandler` 兜底记录日志并返回 `INTERNAL_ERROR`。
  - 参数校验异常映射到 `BAD_REQUEST`。
  - 业务错误优先在 controller/service 返回 `ApiResponse.fail`（与现有风格一致），减少“抛 RuntimeException 当流程控制”。
- Controller 约定：
  - 路径前缀：`/api/v1/...`
  - 公开 GET 白名单在 `SecurityConfig` 配置（`backend/src/main/java/com/eblog/auth/SecurityConfig.java`）。
  - 参数校验：当前以手动校验为主（id/limit/offset）；保持一致。
- 数据库：Flyway migrations 在 `backend/src/main/resources/db/migration/`；新增变更只加新 migration，不改历史文件。
- MyBatis-Plus：查询优先用 `LambdaQueryWrapper`（参考 `backend/src/main/java/com/eblog/user/AuthorController.java`）。
- 命名：类 `PascalCase`；方法/变量 `lowerCamelCase`；常量 `UPPER_SNAKE_CASE`；DTO 可用 `*Request` / `*Response` / `*View`。
- 测试：JUnit Jupiter；静态断言 `import static ...` 放在普通 import 之上（见 `backend/src/test/java/com/eblog/post/TagParserTest.java`）。

## Cursor / Copilot 规则

- 已检查但未发现：`.cursor/rules/`、`.cursorrules`、`.github/copilot-instructions.md`。

## 推荐工作流（代理执行任务时）

1. 明确改动范围（frontend/backend/infra），避免跨层无关改动。
2. 先补/写测试（能测则测），再实现。
3. 前端至少跑：`npm -C frontend run lint` + 相关 `npm -C frontend run test:ci -- ...`。
4. 后端至少跑：`mvn -f backend/pom.xml test`（必要时指定 `-Dtest=...`）。
5. 提交前复查：无 `.env`/密钥/大文件；配置变更写清楚原因。
