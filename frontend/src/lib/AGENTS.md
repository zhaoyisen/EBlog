# FRONTEND LIBS (src/lib)

## OVERVIEW
跨页面共享模块：鉴权、HTTP header 处理、MDX 渲染/安全策略。

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 登录态与请求封装 | `frontend/src/lib/auth/AuthProvider.tsx` | refresh + CSRF；401 自动 refresh；并发 refresh 去重 |
| CSRF/JWT 工具 | `frontend/src/lib/auth/auth.ts` | `getCsrfToken()` 读 `XSRF-TOKEN`；`parseJwt()` 仅用于解析 role |
| 代理 header 清洗 | `frontend/src/lib/http/sanitizeProxyHeaders.ts` | 过滤 `set-cookie` 与 `www-authenticate`（由调用方单独处理） |
| MDX 渲染缓存 | `frontend/src/lib/mdx/renderMdx.tsx` | `compile` + `run`；内存 cache；组件映射 `mdxComponents` |
| MDX 安全策略 | `frontend/src/lib/mdx/remarkSafeMdx.ts` | 禁止 ESM/表达式/原始 HTML；白名单组件与属性 |

## ANTI-PATTERNS
- 不要在 MDX 中放任 `import/export`、表达式或 raw HTML；安全策略会直接抛错。
