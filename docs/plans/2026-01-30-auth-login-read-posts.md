# 登录/注册 + 浏览文章（本地）Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 本地开发环境完成“插入邀请码 -> 发送邮箱码（日志可见）-> 注册 -> 登录 -> 浏览文章列表/详情”的最小闭环。

**Architecture:**
- 浏览器侧统一请求 `/api/v1/...`，通过 Next dev rewrites 代理到 `http://localhost:8080/api/v1/...`。
- Next Server Components 拉文章使用 `INTERNAL_API_BASE=http://localhost:8080` 拼绝对 URL。
- Refresh token 由后端 HttpOnly Cookie 管理；前端仅保存 access token。

**Tech Stack:** Next.js 15 / React 19 / Vitest / Spring Boot 3.2 / Maven / MyBatis-Plus / Flyway / MySQL

---

### Task 0: 基线验证

**Files:** none

**Step 1: 前端 lint**

Run: `npm -C frontend run lint`

Expected: exit code 0

**Step 2: 前端测试**

Run: `npm -C frontend run test:ci`

Expected: PASS

**Step 3: 后端测试**

Run: `mvn -f backend/pom.xml test`

Expected: PASS

### Task 1: 准备邀请码（手动插库）

**Files:** none

**Step 1: 启动后端一次，确保 Flyway 已建表**

Run: `mvn -f backend/pom.xml spring-boot:run`

Expected: Flyway migrate 成功日志，库中存在 `invite_codes` 表

**Step 2: 插入邀请码（本地 MySQL 客户端执行）**

```sql
INSERT INTO invite_codes (code, status, max_uses, used_count, expires_at)
VALUES ('dev-invite-20260130-001', 'ACTIVE', 50, 0, NULL);
```

Expected: 影响 1 行

### Task 2: Dev 模式打印邮箱验证码

**Files:**
- Modify: `backend/src/main/java/com/eblog/auth/EmailCodeService.java`
- Test: `backend/src/test/java/com/eblog/auth/EmailCodeServiceTest.java`

**Step 1: Write the failing test**

在 `EmailCodeServiceTest` 新增测试，验证 `shouldLogRegisterCode(appEnv)`：

```java
@Test
void logsOnlyInDev() {
  assertTrue(EmailCodeService.shouldLogRegisterCode("dev"));
  assertFalse(EmailCodeService.shouldLogRegisterCode("prod"));
  assertFalse(EmailCodeService.shouldLogRegisterCode(""));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=EmailCodeServiceTest test`

Expected: FAIL（方法未实现）

**Step 3: Write minimal implementation**

在 `EmailCodeService` 添加 logger + 方法：

```java
static boolean shouldLogRegisterCode(String appEnv) {
  return "dev".equalsIgnoreCase(appEnv);
}
```

在 `sendRegisterCode` 生成 code 后、hash 前：

```java
if (shouldLogRegisterCode(appEnv)) {
  log.info("register email code: email={}, code={}", email, code);
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=EmailCodeServiceTest test`

Expected: PASS

### Task 3: 修复前端登录字段匹配

**Files:**
- Modify: `frontend/src/app/login/page.tsx`
- Test: `frontend/src/__tests__/login.test.tsx`

**Step 1: Write the failing test**

新增 `login.test.tsx`，mock fetch 返回：

```ts
{ success: true, data: { accessToken: "token-123" } }
```

断言：`localStorage.getItem("access_token") === "token-123"`。

**Step 2: Run test to verify it fails**

Run: `npm -C frontend run test:ci -- src/__tests__/login.test.tsx`

Expected: FAIL（现有实现取 access_token）

**Step 3: Write minimal implementation**

在 `login/page.tsx` 中：

```ts
const json = (await res.json()) as ApiResponse<{ accessToken: string }>;
if (json?.success && json.data) {
  localStorage.setItem("access_token", json.data.accessToken);
}
```

**Step 4: Run test to verify it passes**

Run: `npm -C frontend run test:ci -- src/__tests__/login.test.tsx`

Expected: PASS

### Task 4: 本地 `/api` 代理到后端

**Files:**
- Modify: `frontend/next.config.ts`

**Step 1: Add rewrites**

```ts
async rewrites() {
  return [{ source: "/api/:path*", destination: "http://localhost:8080/api/:path*" }];
}
```

**Step 2: Manual verification**

Run: `npm -C frontend run dev`

Expected: 浏览器请求 `/api/v1/...` 成功命中后端

### Task 5: Server Components 用 INTERNAL_API_BASE

**Files:**
- Modify: `frontend/src/app/posts/page.tsx`
- Modify: `frontend/src/app/posts/[slug]/page.tsx`

**Step 1: Update apiUrl to use INTERNAL_API_BASE**

```ts
const baseRaw = process.env.INTERNAL_API_BASE ?? "http://localhost:8080";
```

**Step 2: Run build**

Run: `npm -C frontend run build`

Expected: PASS

### Task 6: 新增注册页

**Files:**
- Create: `frontend/src/app/register/page.tsx`
- Modify: `frontend/src/app/login/page.tsx`

**Step 1: Create register page**

功能：发送邮箱码 + 注册，成功后跳转 `/login`。

**Step 2: Add a test**

覆盖一个核心分支（如必填校验或成功跳转）。

Run: `npm -C frontend run test:ci -- -t "register"`

Expected: PASS

### Task 7: 最终验证

**Files:** none

**Step 1: Frontend**

Run: `npm -C frontend run lint`

Run: `npm -C frontend run test:ci`

Run: `npm -C frontend run build`

Expected: PASS

**Step 2: Backend**

Run: `mvn -f backend/pom.xml test`

Expected: PASS
