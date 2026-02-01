# EBlog

一个面向开发者的多用户技术博客平台：用于记录问题排查、技术学习过程，也支持多人投稿与互动。

## 技术栈（已确认）

- 后端：Spring Boot + MySQL 8 + MyBatis-Plus
- 前端：Next.js（React SSR），前后端分离
- 对象存储：MinIO（图片/附件）
- 邮件：SMTP（注册邮箱验证码）
- 部署：云服务器 Docker
- 认证：JWT + RefreshToken（Refresh 存库可撤销/轮换）+ HttpOnly Cookie
- 内容：普通用户 Markdown；管理员可用 MDX（仅基础排版组件）
- 审核：发布后异步审核（规则引擎 + 管人工复核队列；大模型后续可插拔）

## 本地开发（占位）

依赖：
- Docker（用于启动 MySQL 与 MinIO）
- Java 17（后端）
- Node.js 20+（前端）

启动流程：

1. 启动依赖服务（MySQL/MinIO）

```bash
docker compose -f infra/docker-compose.yml up -d
```

2. 启动后端（Spring Boot）

```bash
mvn -f backend/pom.xml spring-boot:run
```

3. 启动前端（Next.js dev）

```bash
npm -C frontend install
npm -C frontend run dev
```

验证（任选其一）：
- 后端健康检查：`GET http://localhost:8080/api/v1/health`
- 前端首页：`http://localhost:3000`

## 目录结构

- `backend/`：Spring Boot 后端工程（后续任务生成）
- `frontend/`：Next.js 前端工程（后续任务生成）
- `infra/`：Docker Compose、Nginx 等基础设施
- `docs/`：项目文档（中文）
