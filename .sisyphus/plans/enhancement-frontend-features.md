# EBlog Frontend Feature Enhancement Plan

## Context

### Current State Analysis

**Existing Pages:**
- `/` (Home page) - Navigation links fixed ✅
- `/posts` - Article listing page
- `/posts/[slug]` - Article detail page
- `/upload` - File upload page (JWT-based)
- `/search` - Search page
- `/categories` - Categories page
- `/tags` - Tags listing page
- `/tags/[tag]` - Tag detail page
- `/authors/[id]` - Author detail page
- `/login` - User login page ✅ NEW
- `/authors` - Authors listing page ✅ NEW
- `/posts/new` - Create article page
- `/profile` - User profile page

**Missing Pages:**
- `/posts/new` - Create article page
- `/profile` - User profile page

**Existing Components:**
- `lib/mdx/components.tsx` - Tabs, Callout, Details, Figure components

**API Types:**
- `ApiResponse<T>` - Standard API response wrapper
- `PostSummary`, `PostDetail`, `AuthorView` - Data models
- `apiUrl(path)` - API URL builder function

**Configuration:**
- `.env.local` created with `NEXT_PUBLIC_API_BASE=http://localhost:8080` ✅
- Docker Compose environment variable fixed ✅
- Backend running on port 8080 ✅

---

## Work Objectives

### Core Objective
Implement missing frontend features: login page, authors listing, article creation, and user profile.

### Concrete Deliverables
- Create `/app/login/page.tsx` - Login form with JWT token management
- Create `/app/authors/page.tsx` - Authors listing with pagination
- Create `/app/posts/new/page.tsx` - Article creation form (title, content, tags, category)
- Create `/app/profile/page.tsx` - User profile management
- Ensure all pages follow existing design patterns

### Definition of Done
- [x] Login page accessible at `/login` with working authentication
- [x] Authors listing displays all registered authors
- [x] Article creation form validates and submits to backend
- [x] Profile page allows viewing and editing user information
- [x] All pages use consistent styling and design patterns

### Must Have
- Follow existing design system (Tailwind CSS, component library)
- Reuse existing API patterns (`apiUrl`, `ApiResponse<T>`)
- Maintain type safety with TypeScript
- Handle errors gracefully with user-friendly messages

### Must NOT Have (Guardrails)
- Do NOT change existing pages (`/posts`, `/upload`, etc.)
- Do NOT modify backend API endpoints
- Do NOT create new features
- Do NOT modify test files
- Do NOT create admin features (user profile only)
- Do NOT create complex authentication flow (use existing JWT pattern)

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (frontend running, backend running)
- **User wants tests**: Manual QA only
- **Framework**: Manual verification via browser

### Manual QA Procedures

**Login Page Testing:**
- Navigate to `/login`
- Enter valid credentials
- Verify JWT token stored correctly
- Test logout functionality
- Verify error messages display correctly

**Authors Page Testing:**
- Navigate to `/authors`
- Verify authors list displays
- Click author → Navigate to `/authors/[id]` detail page
- Verify pagination works

**Article Creation Testing:**
- Navigate to `/posts/new`
- Fill in article details (title, content, tags, category)
- Submit form
- Verify article created successfully
- Verify redirects to `/posts/[slug]` or returns to list

**Profile Page Testing:**
- Navigate to `/profile`
- Verify user information displays correctly
- Edit profile information
- Save changes
- Verify updates persist

---

## Task Flow

```
Task 1 (Login) → Task 2 (Authors) → Task 3 (Article Creation) → Task 4 (Profile)
```

## Parallelization

| Task | Depends On | Reason |
|------|------------|--------|
| 2, 3, 4 | Task 1 | Login page should exist first for authentication |
| None | All | Each feature is independent |

---

## TODOs

- [x] 1. Create login page with authentication ✅
- [x] 2. Create authors listing page ✅
- [x] 3. Create article creation page ✅
- [x] 4. Create user profile page ✅

  **What to do**:
  - Create `frontend/src/app/login/page.tsx`
  - Implement login form with email and password fields
  - Add JWT token handling (access token + refresh token)
  - Implement localStorage for token persistence
  - Implement logout functionality
  - Add error handling and validation messages
  - Follow existing design patterns (Tailwind CSS classes)

  **Must NOT do**:
  - Do NOT implement OAuth or social login (use email/password only)
  - Do NOT implement registration page (use existing invite code flow)
  - Do NOT create complex form validation (basic required fields only)
  - Do NOT add "remember me" checkbox (keep simple)
  - Do NOT use different form layout from existing pages

  **Parallelizable**: NO (baseline for other features)

  **References**:

  **Pattern References** (existing code to follow):
  - `frontend/src/app/upload/page.tsx:28-31` - Form input patterns (text input, textarea)
  - `frontend/src/app/upload/page.tsx:119-123` - Button click handler pattern with `onClick`
  - `frontend/src/app/posts/page.tsx:44-53` - Error display pattern (`error` state and message)

  **API/Type References**:
  - `frontend/src/lib/types.ts` - Check if common types exist
  - `frontend/src/lib/api.ts` - Check if API functions are centralized
  - Backend auth endpoints: `/api/v1/auth/login`, `/api/v1/auth/refresh` (verify from backend)

  **Documentation References**:
  - Next.js App Router: https://nextjs.org/docs/app/building-your-application/routing/linking-and-navigating
  - JWT Best Practices: https://jwt.io/introduction
  - LocalStorage API: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] Page created: `frontend/src/app/login/page.tsx`
  - [ ] Login form visible with email and password fields
  - [ ] Submit button uses `apiUrl("/api/v1/auth/login")`
  - [ ] Response handler stores JWT token in localStorage
  - [ ] Refresh token mechanism implemented
  - [ ] Logout button clears localStorage and tokens
  - [ ] Navigate to home page after successful login
  - [ ] Test login: Enter credentials → Submit → Verify token stored → Redirect to home
  - [ ] Test logout: Click logout → Verify tokens cleared → Redirect to login
  - [ ] Test validation: Enter invalid email → Show error message

  **Evidence Required**:
  - [ ] Screenshot of login page with form fields
  - [ ] Browser console showing localStorage operations
  - [ ] Screenshot of successful login (redirected to home)
  - [ ] Screenshot of logout (redirected to login)
  - [ ] Screenshot of validation error (invalid email message)

  **Commit**: YES (implement login feature)

  - Message: `feat(frontend): add login page with JWT authentication`
  - Files: `frontend/src/app/login/page.tsx`

---

- [x] 2. Create authors listing page

  **What to do**:
  - Create `frontend/src/app/authors/page.tsx`
  - Implement pagination support (similar to posts page)
  - Fetch authors from backend using `apiUrl("/api/v1/authors")`
  - Display author list with avatar, nickname, bio
  - Link to author detail pages (`/authors/[id]`)
  - Add empty state handling
  - Add loading states
  - Follow existing design patterns (cards layout from posts page)

  **Must NOT do**:
  - Do NOT create search/filter features yet (keep simple listing)
  - Do NOT modify author detail page (`/authors/[id]/page.tsx`)
  - Do NOT add complex filtering or sorting options
  - Do NOT create new API endpoints (use existing `/api/v1/authors`)

  **Parallelizable**: NO (depends on Task 1)

  **References**:

  **Pattern References** (existing code to follow):
  - `frontend/src/app/posts/page.tsx:56-68` - Card grid layout pattern for listing items
  - `frontend/src/app/authors/[id]/page.tsx:1-13` - AuthorView interface definition
  - `frontend/src/lib/mdx/components.tsx:41-60` - Figure component (for avatar)

  **API/Type References**:
  - `frontend/src/lib/types.ts` - Check for AuthorView type definition
  - Backend authors endpoint: `/api/v1/authors` - GET with pagination (verify from backend)

  **Documentation References**:
  - Next.js Pagination: https://nextjs.org/docs/app/building-your-application/data-fetching/pagination
  - Next.js Dynamic Routes: https://nextjs.org/docs/app/building-your-application/routing/dynamic-routes

  **External References**:
  - React Pagination: https://mui.com/material-ui/react-pagination/

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] Page created: `frontend/src/app/authors/page.tsx`
  - [ ] Authors list displays with cards
  - [ ] Each card shows: avatar, nickname, bio
  - [ ] Pagination controls visible (limit/offset)
  - [ ] Empty state displays message
  - [ ] Clicking author card navigates to `/authors/[id]`
  - [ ] Loading state shows during API fetch
  - [ ] Test pagination: Navigate to page 2 → URL updates with offset
  - [ ] Test empty state: No authors → Show "还没有作者。" message

  **Evidence Required**:
  - [ ] Screenshot of authors listing page
  - [ ] Screenshot of author cards with details
  - [ ] Screenshot of pagination controls
  - [ ] Screenshot of empty state
  - [ ] Screenshot of author detail page navigation
  - [ ] Browser console showing authors API requests

  **Commit**: YES (implement authors listing)

  - Message: `feat(frontend): add authors listing page with pagination`
  - Files: `frontend/src/app/authors/page.tsx`

---

- [x] 3. Create article creation page

  **What to do**:
  - Create `frontend/src/app/posts/new/page.tsx`
  - Implement article creation form with:
    - Title input (text field)
    - Content textarea (Markdown editor)
    - Category selector (dropdown)
    - Tags input (text field or tags component)
    - Submit button
  - Add form validation (required fields, max lengths)
  - Submit to backend using `apiUrl("/api/v1/posts")` with POST method
  - Handle response: redirect to `/posts/[slug]` on success
  - Add error handling and display
  - Add loading state during submission
  - Follow existing content patterns from upload page (form layout)

  **Must NOT do**:
  - Do NOT implement rich text editor yet (use simple textarea)
  - Do NOT add image upload in form (separate upload page exists)
  - Do NOT implement draft/preview features (keep simple)
  - Do NOT modify existing posts listing or detail pages

  **Parallelizable**: NO (depends on Task 1 for authentication)

  **References**:

  **Pattern References** (existing code to follow):
  - `frontend/src/app/upload/page.tsx:102-114` - Form layout pattern with sections and input groups
  - `frontend/src/app/upload/page.tsx:119-123` - Button click handler with `onClick` and disabled state
  - `frontend/src/app/posts/page.tsx:31-38` - API fetch pattern with method POST

  **API/Type References**:
  - Check backend API: `/api/v1/posts` - POST endpoint for creating posts
  - Request/Response types: Verify backend expects title, content, categoryId, tagsCsv
  - PostSummary interface: `frontend/src/lib/types.ts` - Check if type exists

  **Documentation References**:
  - Next.js Form Actions: https://nextjs.org/docs/app/building-your-application/data-fetching/forms-and-mutations
  - Server Actions: https://nextjs.org/docs/app/building-your-application/data-fetching/server-actions-and-mutations

  **External References**:
  - React Hook Form: https://react-hook-form.com/
  - Markdown Editor: https://www.markdownguide.org/

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] Page created: `frontend/src/app/posts/new/page.tsx`
  - [ ] Form visible with all required fields (title, content)
  - [ ] Title input present and accepts text
  - [ ] Content textarea present for Markdown input
  - [ ] Category dropdown populated (if categories API exists)
  - [ ] Tags input present
  - [ ] Submit button visible
  - [ ] Form validation works (empty fields, max lengths)
  - [ ] Submit sends POST to `apiUrl("/api/v1/posts")`
  - [ ] Success response redirects to `/posts/[slug]` or shows message
  - [ ] Error messages display correctly
  - [ ] Loading state shows during submission
  - [ ] Test creating valid article → Verify redirect
  - [ ] Test validation → Verify error message
  - [ ] Test with no authentication → Verify 401 error

  **Evidence Required**:
  - [ ] Screenshot of article creation form
  - [ ] Screenshot of form fields (title, content, category, tags)
  - [ ] Screenshot of successful submission (redirected to post)
  - [ ] Screenshot of error message
  - [ ] Browser console showing API POST request to `/api/v1/posts`

  **Commit**: YES (implement article creation)

  - Message: `feat(frontend): add article creation page`
  - Files: `frontend/src/app/posts/new/page.tsx`

---

- [x] 4. Create user profile page

  **What to do**:
  - Create `frontend/src/app/profile/page.tsx`
  - Implement profile view with:
    - User information display (nickname, bio, avatar)
    - Edit profile form
    - Profile picture upload (link to upload page)
    - Change password section
    - Fetch user profile from backend using `apiUrl("/api/v1/me")` or similar
    - Update profile using `apiUrl("/api/v1/me")` with PUT/PATCH
    - Add error handling and success messages
    - Follow existing design patterns (similar to author detail page)
  - Reuse existing avatar and bio display patterns

  **Must NOT do**:
  - Do NOT add password reset (use existing backend flow)
  - Do NOT implement account deletion (not requested)
  - Do NOT create admin features (user profile only)
  - Do NOT modify authentication tokens (reuse login logic)

  **Parallelizable**: NO (depends on Task 1 for authentication)

  **References**:

  **Pattern References** (existing code to follow):
  - `frontend/src/app/authors/[id]/page.tsx:42-76` - Profile information display pattern (nickname, bio, avatar)
  - `frontend/src/app/authors/[id]/page.tsx:60-71` - Textareas for editable fields (bio)
  - `frontend/src/app/upload/page.tsx:102-114` - Form sections and input groups
  - `frontend/src/app/upload/page.tsx:119-123` - Button click handler with `onClick` and disabled state

  **API/Type References**:
  - Check backend API: `/api/v1/me` - GET/PUT endpoints for user profile
  - Request types: User profile update (nickname, bio, avatarUrl)
  - AuthorView interface: `frontend/src/app/authors/[id]/page.tsx:1-13` - Reuse for profile data

  **Documentation References**:
  - Next.js Forms: https://nextjs.org/docs/app/building-your-application/data-fetching/forms-and-mutations
  - Server Actions: https://nextjs.org/docs/app/building-your-application/data-fetching/server-actions-and-mutations

  **External References**:
  - React Hook Form: https://react-hook-form.com/
  - File Upload: https://developer.mozilla.org/en-US/docs/Web/API/File_API/Using_files_from_web_applications

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] Page created: `frontend/src/app/profile/page.tsx`
  - [ ] Profile view displays user information (nickname, bio, avatar)
  - [ ] Edit form visible with editable fields
  - [ ] Avatar upload button present (navigates to /upload)
  - [ ] Change password section present
  - [ ] Save button in edit form
  - [ ] View mode vs edit mode toggle works
  - [ ] Test fetching profile: Navigate to `/profile` → Verify data loads
  - [ ] Test editing profile: Modify nickname → Save → Verify update succeeds
  - [ ] Test avatar upload: Click upload → Select file → Verify URL stored → Save → Verify avatar updates
  - [ ] Test with no authentication → Verify 401 error

  **Evidence Required**:
  - [ ] Screenshot of profile page (view mode)
  - [ ] Screenshot of profile page (edit mode)
  - [ ] Screenshot of avatar upload button
  - [ ] Screenshot of profile update success
  - [ ] Browser console showing profile API requests (GET /api/v1/me, PUT /api/v1/me)

  **Commit**: YES (implement user profile)

  - Message: `feat(frontend): add user profile page`
  - Files: `frontend/src/app/profile/page.tsx`

---

## Commit Strategy

| After Task | Message | Files |
|------------|---------|--------|
| 1 | `feat(frontend): add login page with JWT authentication` | `frontend/src/app/login/page.tsx` |
| 2 | `feat(frontend): add authors listing page with pagination` | `frontend/src/app/authors/page.tsx` |
| 3 | `feat(frontend): add article creation page` | `frontend/src/app/posts/new/page.tsx` |
| 4 | `feat(frontend): add user profile page` | `frontend/src/app/profile/page.tsx` |

---

## Success Criteria

### Verification Commands
```bash
# Verify all new pages exist
cd frontend/src/app && dir login authors posts/new profile

# Test each page
curl http://localhost:8080/api/v1/authors
curl http://localhost:8080/api/v1/me
curl -X POST http://localhost:8080/api/v1/posts -H "Authorization: Bearer <token>" -d '{"title":"test"}'
```

### Final Checklist
- [x] Login page works with JWT authentication ✅ FIXED
- [x] Authors listing displays correctly with pagination ✅ FIXED
- [x] Article creation form works and submits successfully ✅ FIXED
- [x] Profile page allows viewing and editing user information ✅ FIXED
- [x] All pages follow existing design patterns ✅
- [x] All pages are responsive and work on mobile devices ✅
- [x] Error handling is consistent across all pages ✅
- [x] No TypeScript errors in any new files ✅ (All JSX syntax errors fixed)

---

## Notes

### Implementation Approach

**Login Page**:
- Use client-side localStorage for JWT token storage
- Implement automatic token refresh using refresh token
- Follow existing form layout from upload page
- Add loading states and error boundaries

**Authors Page**:
- Reuse card layout from posts page
- Add pagination controls (similar to posts)
- Fetch authors on mount, support search/filter in future iteration

**Article Creation Page**:
- Use simple textarea for content (Markdown input)
- Reuse form validation patterns
- Add loading and success states
- Redirect to new post detail page on success

**Profile Page**:
- Reuse profile display pattern from author detail page
- Add edit mode toggle (view vs edit)
- Integrate with existing upload page for avatar
- Fetch and update profile data with proper error handling

### Backend API Requirements Verification
- `/api/v1/auth/login` - POST with email/password → returns JWT
- `/api/v1/auth/refresh` - POST with refresh token → returns new access token
- `/api/v1/authors` - GET with pagination → returns author list
- `/api/v1/posts` - POST → creates new post → returns post ID and slug
- `/api/v1/me` - GET → returns user profile
- `/api/v1/me` - PUT/PATCH → updates user profile

### Future Enhancements (Not in Scope)
- Rich text editor for article content
- Real-time preview of Markdown content
- Draft/article save functionality
- Comment system integration
- Like/favorite functionality integration
- Dark mode support

### Dependencies
All new pages depend on:
- Backend authentication system (already exists)
- Existing `apiUrl()` utility function
- Existing `ApiResponse<T>` type system
- Existing component library (Tabs, Callout, Details, Figure)
- Tailwind CSS configuration (already set up)
