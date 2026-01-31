# Auth Email Code Verify Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 锁定并验证 `POST /api/v1/auth/email-code/send-register` 端点可用，避免再出现 404，并完成最小化端到端验证。

**Architecture:** 在后端用 MockMvc 增加一个控制器级测试，确保路由被注册且返回 `ApiResponse.ok(null)`。如果测试失败，再回到控制器/扫描/条件注解定位问题。最后用前端代理做一次手动连通性验证。

**Tech Stack:** Spring Boot 3.2, JUnit Jupiter, MockMvc, Next.js 15

---

### Task 1: 后端路由存在性测试

**Files:**
- Create: `backend/src/test/java/com/eblog/auth/EmailCodeControllerTest.java`

**Step 1: 写一个会在 404 时失败的测试**

```java
package com.eblog.auth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmailCodeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EmailCodeService emailCodeService;

  @MockBean
  private LoginRateLimiter rateLimiter;

  @Test
  void sendRegisterReturnsOk() throws Exception {
    when(rateLimiter.tryConsume("email-code:127.0.0.1:test@example.com")).thenReturn(true);

    mockMvc.perform(post("/api/v1/auth/email-code/send-register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"test@example.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(emailCodeService).sendRegisterCode("test@example.com");
  }
}
```

**Step 2: 运行测试确认当前问题**

Run: `mvn -f backend/pom.xml -Dtest=EmailCodeControllerTest test`

Expected: 如果路由未注册或被拦截，HTTP 404/401 导致测试失败；若已修复，则直接 PASS。

**Step 3: 若失败，做最小修复**

最常见修复点（只改必要项）：

```java
// backend/src/main/java/com/eblog/auth/EmailCodeController.java
@RestController
@RequestMapping("/api/v1/auth/email-code")
public class EmailCodeController {
  // 确保没有 @ConditionalOnBean 等条件注解挡住注册
}
```

**Step 4: 重新运行测试确认通过**

Run: `mvn -f backend/pom.xml -Dtest=EmailCodeControllerTest test`

Expected: PASS

**Step 5: 提交**

```bash
git add backend/src/test/java/com/eblog/auth/EmailCodeControllerTest.java
git commit -m "test: cover email code send-register endpoint"
```

---

### Task 2: 前端代理与端到端连通性验证

**Files:**
- Verify: `frontend/src/app/api/[...path]/route.ts`

**Step 1: 启动后端与前端**

Run: `mvn -f backend/pom.xml spring-boot:run`

Run: `npm -C frontend run dev`

**Step 2: 手动验证前端代理**

在浏览器或 curl 中访问：

```bash
curl -X POST http://localhost:3000/api/v1/auth/email-code/send-register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\"}"
```

Expected: HTTP 200，响应体包含 `"success":true`

**Step 3: 可选快速回归**

Run: `npm -C frontend run lint`

Expected: 0 warnings / 0 errors
