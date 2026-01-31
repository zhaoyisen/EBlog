# 前端鉴权与 /api 代理修正 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 修正 Next.js `/api` 代理路由的类型与 Cookie 转发，并把前端登录态改为“refresh cookie + CSRF + 内存 access token”，移除 `localStorage` 存 token。

**Architecture:** 前端新增 `AuthProvider`（内存保存 access token、封装 `apiFetch`、401 自动 refresh 并重试一次）；后端不改动，沿用现有 `refresh_token` HttpOnly Cookie + `CookieCsrfTokenRepository`；Next `/api/[...path]` 代理确保 `Set-Cookie` 多值正确透传。

**Tech Stack:** Next.js 15 + React 19 + TypeScript + Vitest

---

### Task 1: 写一个会失败的测试（锁定“不再写 localStorage”）

**Files:**
- Modify: `frontend/src/__tests__/login.test.tsx`

**Step 1: 改测试断言为“不写 localStorage”**

把原本的断言：

```ts
expect(localStorage.getItem("access_token")).toBe("token-123");
```

改为：

```ts
const setItemSpy = vi.spyOn(window.localStorage, "setItem");
// ...触发登录...
await waitFor(() => {
  expect(setItemSpy).not.toHaveBeenCalled();
});
```

**Step 2: 运行测试确认失败**

Run:
```bash
npm -C frontend run test:ci -- src/__tests__/login.test.tsx
```

Expected: FAIL（当前实现仍会写入 `localStorage`）。

---

### Task 2: 引入 AuthProvider（内存 token + apiFetch + refresh + CSRF）

**Files:**
- Create: `frontend/src/lib/auth/auth.ts`
- Create: `frontend/src/lib/auth/AuthProvider.tsx`
- Modify: `frontend/src/app/layout.tsx`

**Step 1: 新增 Cookie/CSRF 读取工具**

Create `frontend/src/lib/auth/auth.ts`：

```ts
export function getCookie(name: string): string | null {
  if (typeof document === "undefined") return null;
  const prefix = `${encodeURIComponent(name)}=`;
  const parts = document.cookie.split(";");
  for (const part of parts) {
    const trimmed = part.trim();
    if (trimmed.startsWith(prefix)) {
      return decodeURIComponent(trimmed.slice(prefix.length));
    }
  }
  return null;
}

export function getCsrfToken(): string | null {
  // Spring CookieCsrfTokenRepository 默认 cookie 名
  return getCookie("XSRF-TOKEN");
}
```

**Step 2: 新增 AuthProvider**

Create `frontend/src/lib/auth/AuthProvider.tsx`：

- 内存保存 `accessToken`
- `apiFetch` 默认 `credentials: "include"`
- 若响应为 401：调用一次 `POST /api/v1/auth/refresh`（附带 `X-XSRF-TOKEN` 头）
- refresh 成功后重试原请求 1 次；失败则返回原 401
- 并发 refresh 需要 promise 去重
- 初始化时：先 `GET /api/v1/health`（促使后端下发 `XSRF-TOKEN`），再尝试 refresh

**Step 3: 在 RootLayout 注入 Provider**

Modify `frontend/src/app/layout.tsx`，用 `<AuthProvider>{children}</AuthProvider>` 包裹。

**Step 4: 运行前端测试**

Run:
```bash
npm -C frontend run test:ci
```

Expected: PASS。

---

### Task 3: 修正 Next `/api` 代理 Route Handler（params 类型 + Set-Cookie 透传）

**Files:**
- Modify: `frontend/src/app/api/[...path]/route.ts`

**Step 1: 修正 handler context 类型**

把 `{ params: Promise<{ path: string[] }> }` 改回 `{ params: { path: string[] } }`，并移除 `await params`。

**Step 2: 正确透传多条 Set-Cookie**

若存在 `response.headers.getSetCookie()`，逐条 append 到 NextResponse；否则兜底使用 `response.headers.get("set-cookie")`。

---

### Task 4: 改造登录页：不落盘 token，使用 AuthProvider

**Files:**
- Modify: `frontend/src/app/login/page.tsx`

**Step 1: 替换登录逻辑**

- 删除所有 `localStorage` 读写
- 使用 `useAuth().loginWithPassword(email, password)`
- 成功后跳转首页

**Step 2: 跑 Task 1 的测试**

Run:
```bash
npm -C frontend run test:ci -- src/__tests__/login.test.tsx
```

Expected: PASS。

---

### Task 5: 改造个人中心：用 apiFetch + logout（带 CSRF）

**Files:**
- Modify: `frontend/src/app/profile/page.tsx`

**Step 1: 移除 localStorage 登录判断**

- 使用 `useAuth()` 的 `ready/accessToken`
- `ready && !accessToken` 时再跳转 `/login`

**Step 2: 所有请求走 apiFetch**

- `/api/v1/me`
- `/api/v1/me/password`
- `/api/v1/uploads/presign`

**Step 3: 登出走后端**

- 调用 `POST /api/v1/auth/logout`（带 `X-XSRF-TOKEN`）

---

### Task 6: 改造发文与上传：变成真正“登录后可用”

**Files:**
- Modify: `frontend/src/app/posts/new/page.tsx`
- Modify: `frontend/src/app/upload/page.tsx`

**Step 1: 发文页使用 apiFetch**

- 未登录引导去 `/login`
- `POST /api/v1/posts` 走 `apiFetch`

**Step 2: 上传页移除“手动粘贴 token”**

- `POST /api/v1/uploads/presign` 走 `apiFetch`
- 直传 MinIO 的 PUT 仍用 `uploadUrl`

---

### Task 7: 最小验证

Run:
```bash
npm -C frontend run lint
npm -C frontend run test:ci
npm -C frontend run build
```

Expected: 全部成功。
