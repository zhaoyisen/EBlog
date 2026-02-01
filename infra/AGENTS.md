# INFRA KNOWLEDGE BASE

## OVERVIEW
`infra/` 用 Docker Compose 管理依赖与生产拓扑，Nginx 同域反代：`/` 前端，`/api/` 后端，`/minio/` MinIO。

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 本地依赖（MySQL/MinIO） | `infra/docker-compose.yml` | 仅拉起 MySQL + MinIO（开发） |
| 生产拓扑（含前后端+Nginx） | `infra/docker-compose.prod.yml` | `backend`/`frontend`/`nginx` 组网，端口映射与健康检查在此 |
| 反向代理 | `infra/nginx/nginx.conf` | `/api/` -> `backend:8080`；`/minio/` -> `minio:9000`；`/` -> `frontend:3000` |
| 环境变量模板 | `infra/.env.example` | 复制为 `infra/.env`；不要提交真实值 |

## CONVENTIONS
- 容器内通过服务名互联（`backend`、`frontend`、`mysql`、`minio`、`nginx`）。

## ANTI-PATTERNS
- 不要提交 `infra/.env`（密钥/口令）。
