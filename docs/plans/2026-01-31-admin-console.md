# 管理后台 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为 EBlog 增加基础管理后台页面与操作能力

**Architecture:** Next.js App Router 下新增 `/admin` 路由组；页面均为 client，使用 AuthProvider 的 `apiFetch` 调后端管理接口；统一错误展示与加载态。

**Tech Stack:** Next.js 15 + React 19 + TailwindCSS + Vitest

---

### Task 0: 隔离工作区（worktree）

**Files:**
- None

**Step 1: 创建 worktree**

```bash
ls -d .worktrees 2>/dev/null
git check-ignore -q .worktrees
git worktree add .worktrees/feature-admin-console -b feature/admin-console
```

**Step 2: 运行基线验证**

```bash
npm -C frontend install
npm -C frontend run lint
npm -C frontend run test:ci
npm -C frontend run build
```

**Step 3: 确认基线通过**

Expected: lint/test/build 全部通过。

### Task 1: 管理后台骨架与导航

**Files:**
- Create: `frontend/src/app/admin/page.tsx`
- Create: `frontend/src/app/admin/_components/AdminShell.tsx`
- Create: `frontend/src/app/admin/_components/AdminNav.tsx`
- Test: `frontend/src/__tests__/admin-shell.test.tsx`

**Step 1: Write the failing test**

```tsx
import { render, screen } from "@testing-library/react";
import AdminPage from "../app/admin/page";

vi.mock("../lib/auth/AuthProvider", () => ({
  useAuth: () => ({ accessToken: null, ready: true }),
}));

test("redirects to login when unauthenticated", () => {
  const location = { href: "" } as Location;
  Object.defineProperty(window, "location", { value: location, writable: true });
  render(<AdminPage />);
  expect(window.location.href).toBe("/login");
});
```

**Step 2: Run test to verify it fails**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-shell.test.tsx`

Expected: FAIL with module not found or behavior mismatch.

**Step 3: Write minimal implementation**

```tsx
// AdminShell: 统一布局
// AdminNav: 入口导航
// AdminPage: 使用 useAuth 进行登录检查
```

**Step 4: Run test to verify it passes**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-shell.test.tsx`

Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/app/admin frontend/src/__tests__/admin-shell.test.tsx
git commit -m "feat(frontend): add admin console shell"
```

### Task 2: 用户管理页（列表 + 封禁/解封）

**Files:**
- Create: `frontend/src/app/admin/users/page.tsx`
- Test: `frontend/src/__tests__/admin-users.test.tsx`

**Step 1: Write the failing test**

```tsx
import { render, screen, fireEvent } from "@testing-library/react";
import UsersPage from "../app/admin/users/page";

test("posts ban request with reason", async () => {
  global.fetch = vi.fn().mockResolvedValue({
    ok: true,
    json: async () => ({ success: true, data: [] }),
  } as Response);
  render(<UsersPage />);
  fireEvent.change(screen.getByLabelText("封禁原因"), { target: { value: "spam" } });
  fireEvent.click(screen.getByText("封禁"));
  expect(global.fetch).toHaveBeenCalled();
});
```

**Step 2: Run test to verify it fails**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-users.test.tsx`

Expected: FAIL

**Step 3: Write minimal implementation**

```tsx
// 列表：GET /api/v1/admin/users
// 操作：POST /api/v1/admin/users/ban/{id} + /unban/{id}
```

**Step 4: Run test to verify it passes**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-users.test.tsx`

Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/app/admin/users/page.tsx frontend/src/__tests__/admin-users.test.tsx
git commit -m "feat(frontend): add admin users management"
```

### Task 3: 邀请码页（列表 + 批量生成 + 吊销 + 使用记录）

**Files:**
- Create: `frontend/src/app/admin/invite-codes/page.tsx`
- Test: `frontend/src/__tests__/admin-invite-codes.test.tsx`

**Step 1: Write the failing test**

```tsx
import { render, screen, fireEvent } from "@testing-library/react";
import InviteCodesPage from "../app/admin/invite-codes/page";

test("creates invite codes with count and maxUses", async () => {
  global.fetch = vi.fn().mockResolvedValue({
    ok: true,
    json: async () => ({ success: true, data: { codes: ["abc"] } }),
  } as Response);
  render(<InviteCodesPage />);
  fireEvent.change(screen.getByLabelText("生成数量"), { target: { value: "2" } });
  fireEvent.change(screen.getByLabelText("最大使用次数"), { target: { value: "3" } });
  fireEvent.click(screen.getByText("批量生成"));
  expect(global.fetch).toHaveBeenCalled();
});
```

**Step 2: Run test to verify it fails**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-invite-codes.test.tsx`

Expected: FAIL

**Step 3: Write minimal implementation**

```tsx
// 列表：GET /api/v1/admin/invite-codes
// 生成：POST /api/v1/admin/invite-codes/batch-create
// 吊销：POST /api/v1/admin/invite-codes/revoke
// 使用记录：GET /api/v1/admin/invite-codes/{code}/uses
```

**Step 4: Run test to verify it passes**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-invite-codes.test.tsx`

Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/app/admin/invite-codes/page.tsx frontend/src/__tests__/admin-invite-codes.test.tsx
git commit -m "feat(frontend): add admin invite code management"
```

### Task 4: 审核页（文章/评论队列 + 通过/拒绝 + 审核日志）

**Files:**
- Create: `frontend/src/app/admin/moderation/page.tsx`
- Test: `frontend/src/__tests__/admin-moderation.test.tsx`

**Step 1: Write the failing test**

```tsx
import { render, screen, fireEvent } from "@testing-library/react";
import ModerationPage from "../app/admin/moderation/page";

test("approves post with reason", async () => {
  global.fetch = vi.fn().mockResolvedValue({
    ok: true,
    json: async () => ({ success: true, data: [] }),
  } as Response);
  render(<ModerationPage />);
  fireEvent.change(screen.getByLabelText("审核原因"), { target: { value: "ok" } });
  fireEvent.click(screen.getByText("通过"));
  expect(global.fetch).toHaveBeenCalled();
});
```

**Step 2: Run test to verify it fails**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-moderation.test.tsx`

Expected: FAIL

**Step 3: Write minimal implementation**

```tsx
// 队列：/api/v1/admin/moderation/review-queue
// 评论队列：/api/v1/admin/moderation/review-queue/comments
// 审核：POST /approve/{id} + /reject/{id}
// 日志：GET /audit-logs
```

**Step 4: Run test to verify it passes**

Run: `npm -C frontend run test:ci -- src/__tests__/admin-moderation.test.tsx`

Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/app/admin/moderation/page.tsx frontend/src/__tests__/admin-moderation.test.tsx
git commit -m "feat(frontend): add admin moderation console"
```

### Task 5: 后端返回 403（推荐，最小修正）

**Files:**
- Modify: `backend/src/main/java/com/eblog/user/AdminUserController.java`
- Modify: `backend/src/main/java/com/eblog/moderation/AdminModerationController.java`

**Step 1: Write minimal implementation**

```java
if (!isAdmin()) {
  return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
}
```

**Step 2: Run verification**

Run: `mvn -f backend/pom.xml -DskipTests package`

Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add backend/src/main/java/com/eblog/user/AdminUserController.java backend/src/main/java/com/eblog/moderation/AdminModerationController.java
git commit -m "fix(backend): return forbidden for non-admin admin endpoints"
```

### Task 6: 全量前端验证

**Files:**
- None

**Step 1: 运行前端验证**

```bash
npm -C frontend run lint
npm -C frontend run test:ci
npm -C frontend run build
```

Expected: 全部通过。
