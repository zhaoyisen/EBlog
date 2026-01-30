# Learnings - Frontend Feature Enhancement

*Last Updated: 2026-01-27*

## Existing Pages Analysis

**已存在的页面：**
- `/` - 首页（导航已修复）
- `/posts` - 文章列表
- `/posts/[slug]` - 文章详情
- `/upload` - 文件上传（JWT-based）
- `/search` - 搜索页面
- `/categories` - 分类页面
- `/tags` - 标签列表
- `/tags/[tag]` - 标签详情
- `/authors/[id]` - 作者详情

**缺少的页面：**
- `/login` - 用户登录
- `/authors` - 作者列表
- `/posts/new` - 创建文章
- `/profile` - 个人中心

## Component Library

**已存在的组件**（`lib/mdx/components.tsx`）：
- Tabs - 标签页切换组件
- Callout - 提示信息组件
- Details - 折叠详情组件
- Figure - 图片/图表容器组件

## API Patterns

**类型定义**（`frontend/src/lib/types.ts`）：
- ApiResponse<T> - 标准API响应包装器
- apiUrl(path) - API URL构建函数（已在之前修复中使用）

## Design System

**设计规范**：
- Tailwind CSS用于样式
- 卡片布局：圆角、阴影、背景色
- 响应式设计：移动端适配
- 渐变色：amber/rose主题

## Task 1 Learnings (2026-01-27)

**登录页面实现要点：**
- 使用JWT令牌存储（localStorage）
- 实现刷新令牌机制
- 邮箱/密码表单
- 登出功能（清除令牌）
- 错误处理和验证消息
- 跟随现有upload页面设计模式

**Authors列表页面实现要点：**
- 复用posts页面的卡片布局
- 分页支持（limit/offset）
- 链接到作者详情页面
- 空状态处理
- 加载状态

**文章创建页面实现要点：**
- 复用upload页面的表单布局
- 标题输入框
- Markdown内容编辑器（textarea）
- 分类选择器（下拉菜单）
- 标签输入框
- 提交按钮
- 验证和加载状态
- 提交成功后重定向到文章详情

**个人中心页面实现要点：**
- 复用author详情页面的信息展示
- 编辑模式切换（查看/编辑）
- 头像上传按钮（链接到upload页面）
- 个人信息表单（昵称、简介）
- 保存和取消按钮
- 密码修改区域

## Patterns Observed

**表单模式**：
- 分区布局（section groups）
- 输入字段（text, textarea, select）
- 提交和取消按钮
- 错误消息显示（条件渲染）

**API调用模式**：
- 使用apiUrl()构建完整URL
- 处理ApiResponse<T>包装器
- 错误处理：检查json.success，显示json.error.message
- 加载状态：useState(false/true)

**状态管理**：
- 本地存储：JWT令牌
- 组件状态：useState钩子
- 表单状态：单独state处理

## Gotchas Found

**待发现的问题**：
- 尚未开始实现，待记录

## Future Improvements

- 富文本编辑器（Markdown增强）
- 实时预览功能
- 文章草稿保存
- 评论系统集成
- 点赞/收藏功能
- 暗色模式支持
