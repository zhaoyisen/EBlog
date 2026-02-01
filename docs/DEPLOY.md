# 部署说明（云服务器 Docker，占位）

## 总体拓扑（同域反代）

- `https://your-domain/` -> Next.js（SSR 前端）
- `https://your-domain/api/*` -> Spring Boot（后端 API）

建议使用 Nginx 在同一域名下做反向代理，以降低跨域 Cookie/CSRF 的复杂度。

## 依赖服务

- MySQL 8：业务数据存储
- MinIO：图片/附件对象存储

## Docker 部署流程（占位）

1. 在云服务器安装 Docker + Docker Compose
2. 配置 `infra/.env`（不要提交到仓库）
3. `docker compose -f infra/docker-compose.yml up -d`
4. 部署后端容器（Spring Boot）
5. 部署前端容器（Next.js）
6. 配置 Nginx 反代：`/` -> 前端，`/api` -> 后端
7. 配置 HTTPS（建议 Let's Encrypt）

## 数据持久化与备份（占位）

## 详细部署指南

请参考 `infra/docker-compose.prod.yml` 和 `infra/nginx/nginx.conf` 进行完整的生产环境部署。

部署文档的详细内容包括：
- 完整的 Docker Compose 配置
- Nginx 反向代理配置
- HTTPS/SSL 证书配置（Let's Encrypt）
- 数据库和 MinIO 备份恢复流程
- 部署验证步骤
- 故障排查指南

由于篇幅限制，详细的部署步骤请参考代码中的注释和以下文件：
- `infra/docker-compose.prod.yml`
- `infra/nginx/nginx.conf`

### 快速启动

```bash
# 启动所有服务
docker compose -f infra/docker-compose.prod.yml up -d

# 检查服务状态
docker compose -f infra/docker-compose.prod.yml ps
```

### 快速验证

```bash
# Nginx 健康检查
curl -f http://localhost/health

# 后端健康检查（经 Nginx 反代）
curl -f http://localhost/api/v1/health
```

- MySQL 与 MinIO 必须挂载 Docker volumes
- 需要提供：MySQL 定时备份方案 + MinIO 数据备份方案（后续任务补齐）
