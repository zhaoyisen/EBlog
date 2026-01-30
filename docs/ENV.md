# 环境变量清单（占位）

说明：这里仅列出变量名、用途与示例格式，不包含任何真实值。具体变量会在后续任务落地时补全。

## backend

- `APP_ENV`：运行环境（示例：`dev` / `prod`）
- `SERVER_PORT`：后端端口（示例：`8080`）
- `DB_HOST`：MySQL 主机（示例：`mysql`）
- `DB_PORT`：MySQL 端口（示例：`3306`）
- `DB_NAME`：数据库名（示例：`eblog`）
- `DB_USER`：数据库用户（示例：`eblog`）
- `DB_PASSWORD`：数据库密码（示例：`<REDACTED>`）
- `JWT_ISSUER`：JWT 签发者（示例：`eblog`）
- `JWT_SECRET`：JWT HMAC 密钥（示例：`<REDACTED>`，生产必须设置为高强度随机值）
- `JWT_ACCESS_TTL_SECONDS`：Access Token 有效期（秒）（示例：`900`）
- `JWT_REFRESH_TTL_SECONDS`：Refresh Token 有效期（秒）（示例：`604800`）
- `COOKIE_SECURE`：Cookie 是否开启 Secure（示例：`false` / `true`；生产建议 `true`）
- `SMTP_HOST`：SMTP 主机（示例：`smtp.example.com`）
- `SMTP_PORT`：SMTP 端口（示例：`465` / `587`）
- `SMTP_USER`：SMTP 用户（示例：`noreply@example.com`）
- `SMTP_PASSWORD`：SMTP 密码（示例：`<REDACTED>`）
- `SMTP_FROM`：发件人地址（示例：`noreply@example.com`）
- `EMAIL_CODE_TTL_SECONDS`：邮箱验证码有效期（秒）（示例：`600`）
- `PASSWORD_RESET_TTL_SECONDS`：重置密码令牌有效期（秒）（示例：`1800`）
- `MINIO_ENDPOINT`：MinIO 地址（示例：`http://minio:9000`）
- `MINIO_ACCESS_KEY`：MinIO Access Key（示例：`<REDACTED>`）
- `MINIO_SECRET_KEY`：MinIO Secret Key（示例：`<REDACTED>`）
- `MINIO_BUCKET_PUBLIC`：公开资源桶名（示例：`eblog-public`）
- `MINIO_BUCKET_PRIVATE`：私有资源桶名（示例：`eblog-private`）
- `MINIO_PUBLIC_BASE_URL`：公开访问的基础 URL（示例：`https://cdn.example.com`；不设则使用 `MINIO_ENDPOINT`）
- `UPLOAD_MAX_BYTES`：上传大小限制（字节）（示例：`10485760`）
- `RATE_LIMIT_MODE`：限流存储方式（示例：`memory` / `redis`）

## frontend

- `NEXT_PUBLIC_SITE_URL`：站点 URL（示例：`https://example.com`）
- `NEXT_PUBLIC_API_BASE`：API 基地址（同域反代时可为空或 `/api`）（示例：`/api`）

## infra

- `MYSQL_ROOT_PASSWORD`：MySQL root 密码（示例：`<REDACTED>`）
- `MYSQL_DATABASE`：初始化数据库名（示例：`eblog`）
- `MYSQL_USER`：初始化用户（示例：`eblog`）
- `MYSQL_PASSWORD`：初始化用户密码（示例：`<REDACTED>`）
- `MINIO_ROOT_USER`：MinIO 管理用户（示例：`<REDACTED>`）
- `MINIO_ROOT_PASSWORD`：MinIO 管理密码（示例：`<REDACTED>`）
