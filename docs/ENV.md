# 配置清单（配置文件为主，容器化部署可用环境变量覆盖）

说明：
- 开发期建议优先使用配置文件（便于本地调试与版本可追溯）。
- 容器化部署（见 `infra/docker-compose*.yml`）会通过环境变量注入/覆盖部分运行参数（例如 DB/MinIO/SMTP/COOKIE_SECURE 等）。
- `infra/.env` **不要提交到仓库**；请使用 `infra/.env.example` 作为模板。

## backend

配置文件：`backend/src/main/resources/application.yml`

- `server.port`：后端端口（示例：`8080`）
- `app.env`：运行环境（示例：`dev` / `prod`）
- `app.base-url`：站点基础 URL（示例：`http://localhost:3000`）

- `app.jwt.secret`：JWT HMAC 密钥（示例：`dev-secret-change-me`）
- `app.jwt.issuer`：JWT 签发者（示例：`eblog`）
- `app.jwt.access-ttl-seconds`：Access Token 有效期（秒）
- `app.jwt.refresh-ttl-seconds`：Refresh Token 有效期（秒）
- `app.cookie.secure`：Cookie 是否开启 Secure（`true/false`）

- `app.mail.from`：发件人地址（示例：`noreply@eblog.local`）
- `app.email-code.ttl-seconds`：邮箱验证码有效期（秒）
- `app.password-reset.ttl-seconds`：重置密码令牌有效期（秒）

- `app.worker.interval-seconds`：审核 worker 轮询间隔（秒）
- `app.worker.batch-size`：审核 worker 每批处理数

- `app.moderation.sensitive-words`：敏感词列表（逗号分隔）
- `app.moderation.max-external-links`：外链数量阈值

- `app.upload.max-bytes`：上传大小限制（字节）

- `app.minio.endpoint`：MinIO 地址
- `app.minio.access-key`：MinIO Access Key
- `app.minio.secret-key`：MinIO Secret Key
- `app.minio.bucket-public`：公开资源桶名
- `app.minio.public-base-url`：公开访问基础 URL

- `spring.datasource.url`：数据库连接
- `spring.datasource.username`：数据库用户名
- `spring.datasource.password`：数据库密码
- `spring.mail.host`：SMTP 主机
- `spring.mail.port`：SMTP 端口
- `spring.mail.username`：SMTP 用户
- `spring.mail.password`：SMTP 密码

## frontend

配置文件：`frontend/src/config/appConfig.ts`

- `appConfig.apiBase`：浏览器请求的 API 基地址（示例：`/api`）
- `appConfig.internalApiBase`：服务端请求后端的基地址（示例：`http://localhost:8080`）
- `appConfig.apiProxyTarget`：Next dev rewrites 代理目标（示例：`http://localhost:8080`）
- `appConfig.siteUrl`：站点 URL（示例：`http://localhost:3000`）

## 验证记录

- 2026-01-31：本地联调通过，前端代理调用 `/api/v1/auth/email-code/send-register` 返回 `success=true`。

## Docker/生产环境（env 注入）

生产或容器化运行时，建议通过 `infra/docker-compose.prod.yml` 统一编排：

- 环境变量模板：`infra/.env.example`
- 实际文件：`infra/.env`（本地复制后填写）

常用变量（按 compose 文件可能存在两套命名，详见 `infra/.env.example`）：
- MySQL：`MYSQL_*` 或 `DB_*`
- MinIO：`MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD`
- 邮件：`SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASSWORD`
- Cookie：`COOKIE_SECURE`
