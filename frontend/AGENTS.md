# FRONTEND KNOWLEDGE BASE

## OVERVIEW
`frontend/` 是 Next.js 15（React 19，TypeScript）项目，使用 App Router（`frontend/src/app`）+ Tailwind。测试框架为 Vitest。

## STRUCTURE
```
frontend/
├── package.json
├── next.config.ts
└── src/
    ├── app/                 # App Router 页面与 route handlers
    ├── lib/                 # auth/http/mdx 等跨页面模块
    ├── config/appConfig.ts  # API base 与代理目标
    └── __tests__/           # Vitest tests
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 根布局/Provider 注入 | `frontend/src/app/layout.tsx` | 读 `cookie` 预判是否有 refresh cookie，并传入 `AuthProvider` |
| `/api/*` 代理 | `frontend/src/app/api/[...path]/route.ts` | 代理到后端；关键：`Set-Cookie` 多值透传 |
| 登录态与请求封装 | `frontend/src/lib/auth/AuthProvider.tsx` | refresh cookie + CSRF + 内存 access token；401 自动 refresh 并重试 |
| CSRF cookie 读取 | `frontend/src/lib/auth/auth.ts` | 读取 `XSRF-TOKEN` 并生成 header `X-XSRF-TOKEN` |
| API base 与代理目标 | `frontend/src/config/appConfig.ts` | `apiBase` 通常为 `/api`，dev 代理目标为 `apiProxyTarget` |
| MDX 渲染与安全 | `frontend/src/lib/mdx` | `remarkSafeMdx` 禁止表达式/HTML/import；仅允许少数组件 |
| 测试 | `frontend/src/__tests__` | `vitest.config.ts` 使用 `happy-dom` |

## CONVENTIONS
- **Next dev**：`npm -C frontend run dev` 使用 `--turbopack`。
- **ESLint**：`npm -C frontend run lint` 会 `--max-warnings 0`；但 `next build` 会忽略 ESLint（见 `next.config.ts`）。
- **Auth**：避免 `localStorage`；测试 `frontend/src/__tests__/login.test.tsx` 约束“不会落盘 token”。

## COMMANDS
```bash
npm -C frontend install
npm -C frontend run dev
npm -C frontend run test:ci
npm -C frontend run lint
npm -C frontend run build
```
