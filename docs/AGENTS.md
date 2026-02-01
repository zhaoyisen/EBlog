# DOCS KNOWLEDGE BASE

## OVERVIEW
`docs/` 是中文项目文档与实现计划。`docs/plans/*.md` 往往包含“任务拆解 + 预期命令 + 关键约束”，对理解项目意图很关键。

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 项目概览/技术栈 | `docs/README.md` | 描述后端/前端/对象存储/审核/认证方案 |
| 部署拓扑 | `docs/DEPLOY.md` | 同域反代建议；引用 `infra/docker-compose.prod.yml` 与 `infra/nginx/nginx.conf` |
| 配置清单 | `docs/ENV.md` | 注意与 `infra/.env.example` 的策略差异；改动前先统一口径 |
| 实现计划与决策记录 | `docs/plans` | 例如前端鉴权与 `/api` 代理修正等 |

## CONVENTIONS
- 计划文档里如果出现“锁定测试”“预期失败/通过”“Run 命令”，通常就是该模块的事实约束（比口头约定更可靠）。
