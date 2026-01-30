# Issues - Frontend Feature Enhancement

*Last Updated: 2026-01-27*

## Task 1: 登录页面
- [ ] No issues encountered

## Task 2: Authors列表页面
- [ ] No issues encountered

## Task 3: 文章创建页面
- [ ] No issues encountered

## Task 4: 个人中心页面
- [ ] No issues encountered

## Blocked Issues
- [ ] None currently

## Resolved Issues
- [ ] None currently (no issues encountered yet)

## Known Issues

### Backend API Requirements
**需要确认的API端点**：
- GET /api/v1/authors - Authors列表（分页）
- POST /api/v1/auth/login - 登录（已存在）
- POST /api/v1/auth/refresh - 刷新令牌（已存在）
- GET /api/v1/categories - 获取分类列表（用于创建文章）
- POST /api/v1/posts - 创建文章（已存在）
- GET /api/v1/me - 获取/更新用户信息（待确认）

### 设计模式依赖
**需要复用的组件和模式**：
- ✅ 卡片布局（posts页面已实现）
- ✅ API响应处理（ApiResponse<T>模式已存在）
- ✅ apiUrl()函数（已在前一计划中创建）
- ✅ Tabs组件（lib/mdx/components.tsx已存在）
- ✅ 表单输入模式（upload页面已实现）

### 潜在的技术问题
- [ ] Markdown内容验证（后端如何处理）
- [ ] 分页参数命名（limit vs pageSize）
- [ ] 标签格式（CSV vs JSON）
- [ ] 头像上传集成（如何将upload页面返回的URL更新到profile）

### 待发现的问题
- 开始实现后记录所有遇到的技术挑战和解决方案
