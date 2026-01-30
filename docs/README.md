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

说明：当前仓库处于初始化阶段，后续任务会补齐 backend/frontend/infra 的具体可运行脚手架与命令。

依赖：
- Docker（用于启动 MySQL 与 MinIO）

预计的启动流程（后续补齐）：
1. `docker compose -f infra/docker-compose.yml up -d`
2. 启动后端（后续补齐 Maven/Gradle 命令）
3. 启动前端（后续补齐 pnpm/npm 命令）

## 目录结构

- `backend/`：Spring Boot 后端工程（后续任务生成）
- `frontend/`：Next.js 前端工程（后续任务生成）
- `infra/`：Docker Compose、Nginx 等基础设施
- `docs/`：项目文档（中文）
