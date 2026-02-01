# 认证与 CSRF 验证流程 (QA_AUTH)

本文档描述了如何通过前端 `/api` 代理验证 EBlog 的认证与 CSRF 保护机制。

## 前置条件

1.  **后端服务已启动**：`mvn -f backend/pom.xml spring-boot:run` (默认端口 8080)
2.  **前端服务已启动**：`npm -C frontend run dev` (默认端口 3000)
3.  **代理配置**：确保 `frontend/src/config/appConfig.ts` 中的 `apiBase` 为 `/api`。

所有请求将发送至 `http://localhost:3000/api/...` 以验证 Next.js 代理的正确性。

---

## 验证步骤

### 1. 获取 CSRF 令牌
首先，通过 GET 请求获取 CSRF 令牌。此操作会设置 `XSRF-TOKEN` Cookie。

#### Bash (curl)
```bash
curl -i -c cookies.txt http://localhost:3000/api/v1/auth/csrf
```

#### PowerShell
```powershell
$resp = Invoke-WebRequest -Uri "http://localhost:3000/api/v1/auth/csrf" -SessionVariable sess
$token = ($resp.Content | ConvertFrom-Json).data.token
Write-Host "CSRF Token: $token"
```

**预期结果**：
- 响应状态码：200 OK
- 响应头包含 `Set-Cookie: XSRF-TOKEN=...; Path=/`
- 响应体包含 `{"code":200,"data":{"token":"...","headerName":"X-XSRF-TOKEN",...}}`

---

### 2. 验证登录时的 CSRF 保护 (无 Header)
尝试在不带 `X-XSRF-TOKEN` Header 的情况下进行登录。

#### Bash (curl)
```bash
curl -i -b cookies.txt -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"wrong"}'
```

#### PowerShell
```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/v1/auth/login" `
  -Method Post -WebSession $sess -ContentType "application/json" `
  -Body '{"email":"test@example.com","password":"wrong"}' -SkipHttpErrorCheck
```

**预期结果**：
- 响应状态码：**403 Forbidden** (CSRF 校验失败)

---

### 3. 验证登录时的 CSRF 保护 (带 Header)
带上正确的 `X-XSRF-TOKEN` Header 进行登录。

#### Bash (curl)
```bash
# 提取 Token (假设已在 cookies.txt 中)
TOKEN=$(grep XSRF-TOKEN cookies.txt | tail -n 1 | awk '{print $7}')
curl -i -b cookies.txt -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $TOKEN" \
  -d '{"email":"test@example.com","password":"wrong"}'
```

#### PowerShell
```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/v1/auth/login" `
  -Method Post -WebSession $sess -ContentType "application/json" `
  -Headers @{"X-XSRF-TOKEN"=$token} `
  -Body '{"email":"test@example.com","password":"wrong"}' -SkipHttpErrorCheck
```

**预期结果**：
- 响应状态码：**401 Unauthorized** (凭据错误) 或 **200 OK** (如果凭据正确)
- **关键点**：不应返回 403。

---

### 4. 验证 Token 刷新 (CSRF 豁免)
`/api/v1/auth/refresh` 应当豁免 CSRF 检查，因为它依赖于 `SameSite=Strict` 的 HttpOnly Cookie。

#### Bash (curl)
```bash
curl -i -b cookies.txt -X POST http://localhost:3000/api/v1/auth/refresh
```

#### PowerShell
```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/v1/auth/refresh" `
  -Method Post -WebSession $sess -SkipHttpErrorCheck
```

**预期结果**：
- 响应状态码：**401 Unauthorized** (因为没有有效的 refresh_token cookie) 或 **200 OK**。
- **关键点**：不应返回 403。

---

### 5. 验证登出 (CSRF 豁免)
`/api/v1/auth/logout` 同样应当豁免 CSRF 检查。

#### Bash (curl)
```bash
curl -i -b cookies.txt -X POST http://localhost:3000/api/v1/auth/logout
```

#### PowerShell
```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/v1/auth/logout" `
  -Method Post -WebSession $sess -SkipHttpErrorCheck
```

**预期结果**：
- 响应状态码：**200 OK**
- 响应头包含 `Set-Cookie: refresh_token=; Max-Age=0; ...` (清除 Cookie)

---

## 观察多重 Set-Cookie Header

在登录成功或刷新 Token 时，后端可能会同时返回多个 `Set-Cookie`（例如 `XSRF-TOKEN` 和 `refresh_token`）。

使用 `curl -i` 时，请检查输出中是否存在多行 `Set-Cookie`：
```text
HTTP/1.1 200 OK
Set-Cookie: XSRF-TOKEN=...; Path=/
Set-Cookie: refresh_token=...; Max-Age=...; HttpOnly; SameSite=Strict; Path=/
```
前端代理 (`frontend/src/app/api/[...path]/route.ts`) 必须正确透传所有 `Set-Cookie` 头部。

---

## 已知限制

1.  **登录成功**：验证 200 OK 需要数据库中存在对应用户。
2.  **Cookie 域**：在 localhost 开发环境下，Cookie 通常绑定到 `localhost`。
3.  **HTTPS**：如果后端配置了 `app.cookie.secure=true`，则在非 HTTPS 环境下浏览器可能不会存储 Cookie，但 `curl` 仍可通过 `-c/-b` 模拟。
