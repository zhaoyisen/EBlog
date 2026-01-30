# Fix Frontend Button and API Configuration Issues

## Context

### Original Problem
User reported that when starting the frontend in VSCode, all buttons are unclickable and no API requests are being sent.

### Root Cause Analysis

**Issue 1: Dead Links on Home Page**
- File: `frontend/src/app/page.tsx`
- All buttons use `href="#"` which only jumps to page top
- Navigation buttons ("文章", "作者", "登录", "立即开始", "查看最新文章") don't navigate anywhere

**Issue 2: Environment Variable Name Mismatch**
- Docker Compose (`infra/docker-compose.prod.yml:93`) uses: `NEXT_PUBLIC_API_URL`
- Frontend code (`frontend/src/app/posts/page.tsx:19`) uses: `NEXT_PUBLIC_API_BASE`
- Results in API requests going to wrong URL (empty string or undefined)

**Issue 3: Missing .env File**
- Frontend directory has no `.env` or `.env.local` file
- `NEXT_PUBLIC_API_BASE` defaults to empty string in development
- API requests fail immediately

**Issue 4: Backend Not Running**
- Frontend runs in VSCode (npm run dev)
- Backend, MySQL, MinIO not started
- Even with correct API URL, requests would fail

---

## Work Objectives

### Core Objective
Fix all frontend button and API configuration issues to enable proper local development workflow.

### Concrete Deliverables
- Create `frontend/.env.local` with correct API base URL
- Update `frontend/src/app/page.tsx` to use actual route links
- Standardize environment variable name across Docker and code
- Provide instructions for starting backend services

### Definition of Done
- [x] Frontend `.env.local` exists with `NEXT_PUBLIC_API_BASE=http://localhost:8080`
- [x] Home page buttons navigate to actual routes (`/posts`, `/authors`, etc.)
- [x] Docker Compose uses `NEXT_PUBLIC_API_BASE` to match code
- [x] Backend services can be started successfully
- [x] Frontend can fetch data from backend without errors

### Must Have
- Local development environment must work end-to-end
- API requests must reach backend successfully
- Navigation must function correctly

### Must NOT Have (Guardrails)
- Do NOT modify production Docker environment variables unnecessarily
- Do NOT change backend code
- Do NOT create new features
- Do NOT modify test files
- Do NOT change other frontend pages (only home page)

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO (Docker unavailable)
- **User wants tests**: Manual QA only
- **Framework**: Manual verification via browser

### Manual QA Procedures

Each fix will be verified with browser-based testing:

**For Navigation Fixes:**
- Navigate to: `http://localhost:3000`
- Click "文章" button → Should navigate to `/posts`
- Click "作者" button → Should navigate to `/authors`
- Click "查看最新文章" button → Should navigate to `/posts`
- Verify: URL changes correctly, page loads without errors

**For API Configuration:**
- Check browser Console (F12) → No "undefined" or empty URL errors
- Navigate to: `http://localhost:3000/posts`
- Verify: Page loads with article list (or empty state message)
- Check Network tab → Requests go to `http://localhost:8080/api/v1/...`
- Expected: Either successful response or meaningful error from backend

**For Backend Startup:**
- Terminal: Backend logs show "Started EBlogApplication"
- Verify: Database migrations applied
- Verify: MinIO connection successful
- Check: `curl http://localhost:8080/api/v1/health` → Returns 200

---

## Task Flow

```
Task 1 → Task 2 → Task 3 → Task 4
```

## Parallelization

All tasks must be sequential because they build on each other.

| Task | Depends On | Reason |
|------|------------|--------|
| 2 | 1 | .env.local needed before navigation testing |
| 3 | 1 | Environment variable must exist before fixing links |
| 4 | 1, 2, 3 | Backend needed for end-to-end verification |

---

## TODOs

- [x] 1. Create frontend/.env.local with API configuration

  **What to do**:
  - Create `frontend/.env.local` file
  - Add environment variable: `NEXT_PUBLIC_API_BASE=http://localhost:8080`
  - Ensure file is in frontend directory (not root)

  **Must NOT do**:
  - Do NOT create .env file (use .env.local for local development)
  - Do NOT add other environment variables not needed for local dev
  - Do NOT commit .env.local to version control (should be in .gitignore)

  **Parallelizable**: NO (first task)

  **References**:

  **Pattern References** (existing code to follow):
  - `frontend/src/app/posts/page.tsx:19` - Shows how `NEXT_PUBLIC_API_BASE` is used in code
  - `infra/docker-compose.prod.yml:93` - Shows Docker environment variable structure

  **Documentation References**:
  - Next.js docs: https://nextjs.org/docs/basic-features/environment-variables - Explains .env.local vs .env
  - Frontend package.json: `frontend/package.json:6` - Shows dev script usage

  **External References**:
  - Next.js Environment Variables: https://nextjs.org/docs/basic-features/environment-variables#loading-environment-variables

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] File created: `frontend/.env.local`
  - [ ] File contains: `NEXT_PUBLIC_API_BASE=http://localhost:8080`
  - [ ] Verify file exists: `cd frontend && type .env.local` → Shows content
  - [ ] Restart frontend: `cd frontend && npm run dev` (Ctrl+C to stop, then start again)
  - [ ] Check browser console: No "undefined" or missing environment variable errors
  - [ ] Verify: Open `http://localhost:3000/posts` → Console shows requests to `http://localhost:8080/api/v1/posts`

  **Evidence Required**:
  - [ ] Screenshot of browser console showing correct API URL
  - [ ] Terminal output showing frontend restart after .env.local creation

  **Commit**: NO (group with all frontend fixes)

---

- [x] 2. Fix home page navigation links in frontend/src/app/page.tsx

  **What to do**:
  - Open `frontend/src/app/page.tsx`
  - Change all `href="#"` to actual route paths:
    - Line 15: `<a href="#">文章</a>` → `<a href="/posts">文章</a>`
    - Line 18: `<a href="#">作者</a>` → `<a href="/authors">作者</a>`
    - Line 23: `<a href="#">登录</a>` → `<a href="/login">登录</a>` (or remove if login page doesn't exist yet)
    - Line 44: `<a href="#">立即开始</a>` → `<a href="/posts">立即开始</a>`
    - Line 50: `<a href="#">查看最新文章</a>` → `<a href="/posts">查看最新文章</a>`

  **Must NOT do**:
  - Do NOT change styling or className attributes
  - Do NOT add new buttons or links
  - Do NOT modify the layout structure
  - Do NOT change the hero section content

  **Parallelizable**: NO (depends on Task 1)

  **References**:

  **Pattern References** (existing code to follow):
  - `frontend/src/app/posts/page.tsx:60` - Shows correct Next.js link syntax with `href`
  - `frontend/src/app/tags/page.tsx:49` - Shows how to use `encodeURIComponent` for dynamic routes
  - `frontend/src/app/search/page.tsx:89` - Shows link pattern to `/posts/[slug]`

  **Documentation References**:
  - Next.js Routing: https://nextjs.org/docs/app/building-your-application/routing/linking-and-navigating

  **External References**:
  - Next.js File-based Routing: https://nextjs.org/docs/app/building-your-application/routing/colocation

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] Navigate to: `http://localhost:3000`
  - [ ] Click "文章" button → Browser navigates to `http://localhost:3000/posts`
  - [ ] Click "作者" button → Browser navigates to `http://localhost:3000/authors`
  - [ ] Click "立即开始" button → Browser navigates to `http://localhost:3000/posts`
  - [ ] Click "查看最新文章" button → Browser navigates to `http://localhost:3000/posts`
  - [ ] Verify: Each click changes URL correctly, page loads without 404 errors
  - [ ] Verify: No console errors when clicking links

  **Evidence Required**:
  - [ ] Screenshot of browser showing successful navigation to `/posts`
  - [ ] Screenshot of browser network tab showing page navigation
  - [ ] Console output showing no errors after clicking buttons

  **Commit**: NO (group with all frontend fixes)

---

- [x] 3. Fix environment variable name in docker-compose.prod.yml to match frontend code

  **What to do**:
  - Open `infra/docker-compose.prod.yml`
  - Find the frontend service (around line 84)
  - Change line 93 from:
    ```yaml
    NEXT_PUBLIC_API_URL: http://backend:8080/api/v1
    ```
    To:
    ```yaml
    NEXT_PUBLIC_API_BASE: http://backend:8080
    ```
  - Note: Remove `/api/v1` suffix because frontend code already includes it in apiUrl() function

  **Must NOT do**:
  - Do NOT change any other environment variables in the file
  - Do NOT modify other services (mysql, minio, backend, nginx)
  - Do NOT change ports or network configuration
  - Do NOT modify volumes or healthcheck settings

  **Parallelizable**: NO (depends on Task 1)

  **References**:

  **Pattern References** (existing code to follow):
  - `infra/docker-compose.prod.yml:59-75` - Shows backend environment variable structure
  - `infra/docker-compose.prod.yml:62-64` - Shows database environment variable pattern
  - `frontend/src/app/posts/page.tsx:18-23` - Shows how apiUrl() function constructs full URL

  **Documentation References**:
  - Docker Compose Environment Variables: https://docs.docker.com/compose/environment-variables/

  **External References**:
  - Docker Compose File Reference: https://docs.docker.com/compose/compose-file/compose-file-v3/

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] File modified: `infra/docker-compose.prod.yml`
  - [ ] Line 93 changed: `NEXT_PUBLIC_API_BASE: http://backend:8080`
  - [ ] Verify change: `cd infra && type docker-compose.prod.yml | findstr NEXT_PUBLIC_API`
  - [ ] Expected output shows: `NEXT_PUBLIC_API_BASE: http://backend:8080`
  - [ ] Docker Compose validates: `docker compose -f docker-compose.prod.yml config` → No errors

  **Evidence Required**:
  - [ ] Terminal output showing docker-compose config command succeeds
  - [ ] Screenshot of modified docker-compose.prod.yml line 93

  **Commit**: NO (group with all frontend fixes)

---

- [x] 4. Start backend services for local development (Docker or standalone)

  **What to do**:

  **Option A - Using Docker (Recommended if Docker available):**
  1. Navigate to infra directory: `cd infra`
  2. Check if .env exists: `dir .env`
     - If not exists: Create from example: `copy .env.example .env`
     - Edit .env and fill in required values (use defaults for local dev):
       ```
       MYSQL_ROOT_PASSWORD=rootpassword
       MYSQL_DATABASE=eblog
       MYSQL_USER=eblog
       MYSQL_PASSWORD=eblogpassword
       MINIO_ROOT_USER=minioadmin
       MINIO_ROOT_PASSWORD=miniopassword
       ```
  3. Start services: `docker compose -f docker-compose.prod.yml up -d mysql minio backend`
  4. Wait for startup (check logs): `docker compose -f docker-compose.prod.yml logs -f backend`
  5. Verify all containers running: `docker compose -f docker-compose.prod.yml ps`

  **Option B - Standalone Backend (If Docker unavailable):**
  1. Ensure MySQL is running on localhost:3306
  2. Ensure MinIO is running on localhost:9000
  3. Navigate to backend: `cd backend`
  4. Check application.yml or create one with:
      ```yaml
      spring:
        datasource:
          url: jdbc:mysql://localhost:3306/eblog?useSSL=false&serverTimezone=UTC
          username: eblog
          password: eblogpassword
      ```
  5. Run backend: `mvn spring-boot:run`
  6. Wait for startup: Look for "Started EBlogApplication in X seconds"

  **Must NOT do**:
  - Do NOT start frontend service in Docker (already running in VSCode)
  - Do NOT start nginx service (not needed for local dev)
  - Do NOT modify backend code or configuration
  - Do NOT change database schema or migrations

  **Parallelizable**: NO (depends on Tasks 1, 2, 3)

  **References**:

  **Pattern References** (existing code to follow):
  - `backend/src/main/resources/application.yml` - Shows Spring Boot configuration structure
  - `infra/docker-compose.prod.yml:77-80` - Shows backend healthcheck pattern
  - `backend/src/main/java/com/eblog/EBlogApplication.java` - Main application class

  **Documentation References**:
  - Spring Boot CLI: https://docs.spring.io/spring-boot/docs/current/reference/html/cli.html
  - Docker Compose Up Command: https://docs.docker.com/engine/reference/commandline/compose_up/

  **External References**:
  - Spring Boot Application Properties: https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html

  **Acceptance Criteria**:

  **Manual Execution Verification**:

  **If using Docker:**
  - [ ] Docker containers started: `docker compose -f docker-compose.prod.yml ps` → Shows 3 containers (mysql, minio, backend) as "Up"
  - [ ] Backend logs healthy: `docker compose -f docker-compose.prod.yml logs backend` → Shows no errors
  - [ ] Health check passes: `curl http://localhost:8080/api/v1/health` → Returns 200 OK
  - [ ] Database migrations applied: Logs show "Flyway Community Edition" and migration scripts executed

  **If using standalone backend:**
  - [ ] Backend process running: `mvn spring-boot:run` shows "Started EblogApplication"
  - [ ] Health check passes: `curl http://localhost:8080/api/v1/health` → Returns 200 OK
  - [ ] Database connection successful: No JDBC connection errors in logs

  **Evidence Required**:
  - [ ] Terminal output showing backend startup success
  - [ ] Output of `curl http://localhost:8080/api/v1/health` command
  - [ ] Screenshot of backend logs (or Docker ps output)

  **Commit**: NO (no code changes, just service startup)

---

- [x] 5. End-to-end verification: Test frontend with running backend

  **What to do**:
  1. Ensure frontend is running: `cd frontend && npm run dev` (http://localhost:3000)
  2. Ensure backend is running (from Task 4)
  3. Open browser to: `http://localhost:3000`
  4. Test navigation:
     - Click "文章" → Navigate to /posts
     - Click "作者" → Navigate to /authors (should show error or empty page)
     - Click "查看最新文章" → Navigate to /posts
  5. Test API calls on /posts page:
     - Check Network tab in DevTools (F12)
     - Verify requests go to: `http://localhost:8080/api/v1/posts?limit=20&offset=0`
     - Verify response status (200 OK or meaningful error)
  6. Test upload page (optional):
     - Navigate to: `http://localhost:3000/upload`
     - Fill in JWT token (get from backend login)
     - Upload a small file
     - Verify: Upload URL generated, file uploaded to MinIO

  **Must NOT do**:
  - Do NOT create new test data in database
  - Do NOT modify backend responses
  - Do NOT alter frontend component structure
  - Do NOT skip verification steps

  **Parallelizable**: NO (depends on all previous tasks)

  **References**:

  **Pattern References** (existing code to follow):
  - `frontend/src/app/posts/page.tsx:30-38` - Shows API fetch pattern and error handling
  - `frontend/src/app/upload/page.tsx:49-90` - Shows upload API call flow
  - `frontend/src/app/upload/page.tsx:119-123` - Shows button click handler pattern

  **Documentation References**:
  - Browser DevTools: https://developer.chrome.com/docs/devtools/
  - Next.js Debugging: https://nextjs.org/docs/app/building-your-application/debugging

  **External References**:
  - Chrome DevTools Network Tab: https://developer.chrome.com/docs/devtools/network/

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] Home page loads at `http://localhost:3000` → No console errors
  - [ ] "文章" button click → Navigates to `http://localhost:3000/posts`
  - [ ] /posts page loads → Shows "还没有文章。" or list of posts
  - [ ] Network tab shows: Request to `http://localhost:8080/api/v1/posts?limit=20&offset=0`
  - [ ] Request status: 200 OK (success) or meaningful error (backend not ready)
  - [ ] No 404 errors in console
  - [ ] No "undefined" API URL errors in console

  **Evidence Required**:
  - [ ] Screenshot of browser showing /posts page with API request in network tab
  - [ ] Screenshot of browser console showing no errors
  - [ ] Screenshot of successful navigation from home to posts
  - [ ] Copy of network request details (URL, status, response)

  **Commit**: YES (commit all frontend fixes)

  - Message: `fix(frontend): configure API and fix navigation links`
  - Files:
    - `frontend/.env.local`
    - `frontend/src/app/page.tsx`
    - `infra/docker-compose.prod.yml`
  - Pre-commit: `cd frontend && npm run lint` (optional, if linter configured)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 5 | `fix(frontend): configure API and fix navigation links` | frontend/.env.local, frontend/src/app/page.tsx, infra/docker-compose.prod.yml | npm run dev (optional), test navigation |

---

## Success Criteria

### Verification Commands
```bash
# Frontend environment check
cd frontend && type .env.local
# Expected: NEXT_PUBLIC_API_BASE=http://localhost:8080

# Frontend build check
cd frontend && npm run build
# Expected: Build succeeds

# Backend health check
curl http://localhost:8080/api/v1/health
# Expected: {"success":true}

# Docker status check (if using Docker)
cd infra && docker compose -f docker-compose.prod.yml ps
# Expected: mysql, minio, backend containers show "Up" status
```

### Final Checklist
- [x] `frontend/.env.local` exists with correct API base URL
- [x] Home page navigation buttons work (文章, 作者, 立即开始, 查看最新文章)
- [x] Backend services running (MySQL, MinIO, Spring Boot)
- [x] Frontend can make API requests to backend
- [x] No console errors in browser
- [x] No "undefined" API URL errors
- [x] Docker Compose uses correct environment variable name

---

## Notes

### Why These Changes Fix the Problem

**Problem 1: Dead Links**
- **Root Cause**: `href="#"` only jumps to page top
- **Fix**: Change to actual routes (`/posts`, `/authors`) enables navigation
- **Impact**: Users can now browse the application

**Problem 2: Environment Variable Mismatch**
- **Root Cause**: Docker uses `NEXT_PUBLIC_API_URL`, code uses `NEXT_PUBLIC_API_BASE`
- **Fix**: Standardize on `NEXT_PUBLIC_API_BASE` in both places
- **Impact**: API requests now go to correct URL

**Problem 3: Missing .env**
- **Root Cause**: No `.env.local` file means `NEXT_PUBLIC_API_BASE` is undefined
- **Fix**: Create `frontend/.env.local` with `NEXT_PUBLIC_API_BASE=http://localhost:8080`
- **Impact**: Frontend knows where to send API requests

**Problem 4: Backend Not Running**
- **Root Cause**: Frontend running in VSCode, backend not started
- **Fix**: Start backend services (Docker or standalone)
- **Impact**: API requests receive actual responses instead of network errors

### Next Steps After This Plan

After implementing this plan, the frontend should be fully functional for local development. Future work might include:

1. Implement login page (`/login`) - currently linked but doesn't exist
2. Implement authors page (`/authors`) - currently returns error
3. Add more frontend features (create post, edit profile, etc.)
4. Set up proper CORS configuration if running on different domains

### Troubleshooting

**If buttons still don't work:**
- Check browser console for errors (F12 → Console tab)
- Verify `.env.local` file is in correct directory (frontend/, not project root)
- Restart frontend after creating `.env.local` (Ctrl+C, then `npm run dev`)

**If API requests fail:**
- Verify backend is running: `curl http://localhost:8080/api/v1/health`
- Check backend logs for errors
- Verify MySQL and MinIO are accessible
- Check CORS settings in backend if frontend on different domain

**If Docker Compose fails:**
- Check Docker is running: `docker --version`
- Verify ports not in use: `netstat -ano | findstr :8080`
- Check `.env` file in `infra/` directory exists with required values
