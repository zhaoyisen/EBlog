# Content Metadata Refactor: Real Data & Management

## TL;DR

> **Quick Summary**: 将首页“热门标签/活跃作者”替换为真实后端数据；重构发文体验，分类改为必选，标签支持选择+新建；新增分类/标签的后台管理功能。
> 
> **Deliverables**:
> - DB: `categories`, `tags` 表及数据迁移。
> - API: 热门标签/作者接口，分类/标签管理接口。
> - UI: 发文页使用 `react-select`，新增后台管理页面。
> 
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 2 waves

---

## Context

### Original Request
1. 首页 Sidebar 数据（标签、作者）需来自真实后端。
2. 发文时：分类做成 Select（必选），标签做成 Creatable Select（可选/新建）。
3. 后台管理：增加分类和标签的维护功能。

### Technical Strategy
- **Hybrid Schema**: 保持 `posts.tags_csv` 以最小化改动，但新增 `tags` 和 `categories` 表作为元数据字典。
- **Sync Logic**: 发文/更新文章时，同步更新字典表中的 `post_count`。
- **Migration**: 启动时（Flyway）将现有数据提取到字典表。

---

## Work Objectives

### Core Objective
实现内容元数据的标准化管理和真实数据展示。

### Concrete Deliverables
1.  **Backend**:
    -   Migration: Create `categories`, `tags`.
    -   Logic: Extract existing data to popuate tables.
    -   API: `TagController` (Real popular), `UserController` (Active), `AdminController` (CRUD).
2.  **Frontend**:
    -   Sidebar: Connect to real APIs.
    -   Editor: Replace inputs with Select components.
    -   Admin: New management pages.

---

## Verification Strategy
- **Backend**: `curl` 验证管理接口增删改查。
- **Frontend**: 手动验证发文流程，确认新标签能自动创建。

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Backend & DB):
├── Task 1: DB Schema & Migration [Backend]
├── Task 2: Public Data APIs (Popular/Active) [Backend]
└── Task 3: Admin Management APIs [Backend]

Wave 2 (Frontend Integration):
├── Task 4: Sidebar Real Data Integration [Frontend]
├── Task 5: Post Editor Upgrade (Select Components) [Frontend]
└── Task 6: Admin Management Pages [Frontend]

Critical Path: Task 1 → Task 2/3 → Task 4/5/6
```

---

## TODOs

### Wave 1: Backend Core

- [x] 1. Database Schema & Migration
  **What to do**:
  - Create `V17__metadata_tables.sql`.
  - Tables: `categories` (id, name, slug, post_count), `tags` (id, name, slug, post_count).
  - Migration script logic: 
    - Insert distinct `category` from posts into `categories`.
    - (Tricky in SQL) Insert distinct tags from `tags_csv` into `tags`. *Note: SQL splitting is hard. Might need a Java startup task or simplified SQL approach (e.g. just create tables, populate lazily or use a stored procedure if MySQL 8).*
    - **Decision**: Just create tables. Populate `post_count` via a startup runner (`MetadataSyncRunner`) to ensure accuracy without complex SQL.

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`git-master`]

- [x] 2. Public Data APIs
  **What to do**:
  - Update `TagController`: `listPopular` -> query `tags` table order by `post_count`.
  - Update `UserController`: `listActive` -> query users order by (select count(*) from posts...).
  - Create `MetadataSyncRunner`: `@Component` implementing `CommandLineRunner` to scan posts and populate `categories`/`tags` tables on startup (idempotent).

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`git-master`]

- [x] 4. Sidebar Real Data Integration
  **What to do**:
  - Update `frontend/src/app/page.tsx` (Sidebar) to fetch `/api/v1/tags/popular` and `/api/v1/users/active`.
  - Remove Mock Data.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Actual Result**: Completed via manual edits after file corruption.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

- [ ] 5. Post Editor Upgrade
  **What to do**:
  - Install `react-select` (or `cmdk`).
  - Create `CategorySelect` component (fetch /categories).
  - Create `TagSelect` component (Creatable, fetch /tags).
  - Update `PostEditor.tsx` / `posts/new/page.tsx`.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

- [ ] 6. Admin Management Pages
  **What to do**:
  - Create `/admin/categories/page.tsx`: List + Create/Edit Modal.
  - Create `/admin/tags/page.tsx`: List + Delete.
  - Update Admin Sidebar.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Actual Result**: Completed successfully.
  - Mock data removed and replaced with real API calls.
  - Loading states added for sidebar data.
  - Verified via direct edit.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Actual Result**: Agent claimed complete but no changes detected in page.tsx. The file still uses Mock data and does not call the required APIs.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

- [ ] 5. Post Editor Upgrade
  **What to do**:
  - Install `react-select` (or `cmdk`).
  - Create `CategorySelect` component (fetch /categories).
  - Create `TagSelect` component (Creatable, fetch /tags).
  - Update `PostEditor.tsx` / `posts/new/page.tsx`.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

- [ ] 6. Admin Management Pages
  **What to do**:
  - Create `/admin/categories/page.tsx`: List + Create/Edit Modal.
  - Create `/admin/tags/page.tsx`: List + Delete.
  - Update Admin Sidebar.

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

---

## Commit Strategy
| Task | Message |
|------|---------|
| 1+2 | `feat(backend): implement metadata tables and sync logic` |
| 3 | `feat(backend): add admin apis for metadata` |
| 4 | `feat(ui): connect sidebar to real data` |
| 5 | `feat(ui): upgrade post editor with smart selects` |
| 6 | `feat(admin): add metadata management pages` |
