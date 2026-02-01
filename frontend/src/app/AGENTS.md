# FRONTEND ROUTES (src/app)

## OVERVIEW
Next.js App Router：目录即路由，`page.tsx` 提供 UI；`api/**/route.ts` 提供 Route Handler。

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 首页 | `frontend/src/app/page.tsx` | Client Component；包含导航与登录态 UI |
| 登录/注册 | `frontend/src/app/login/page.tsx`、`frontend/src/app/register/page.tsx` | 使用 `useAuth()` 进行登录 |
| 个人中心 | `frontend/src/app/profile/page.tsx` | 所有请求走 `apiFetch`；登出调用后端 |
| 文章列表/详情 | `frontend/src/app/posts/page.tsx`、`frontend/src/app/posts/[slug]/page.tsx` | 读取后端公开接口 |
| 后台 | `frontend/src/app/admin/*` | 需 ADMIN 角色；UI 组件在 `_components` |
| API 代理 | `frontend/src/app/api/[...path]/route.ts` | 将浏览器 `/api/...` 转发到后端 `/api/...` |

## CONVENTIONS
- `"use client"`：仅在需要 hooks/浏览器 API 的页面加；否则保持 Server Component。
- Route Handler 中要注意 `Set-Cookie` 多值 header 的透传（见 `route.ts`）。
