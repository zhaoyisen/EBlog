# BACKEND KNOWLEDGE BASE

## OVERVIEW
`backend/` 是 Spring Boot 3.2（Java 17）服务：Spring MVC + Spring Security + MyBatis-Plus + Flyway，提供 `/api/v1/*`。

## STRUCTURE
```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/eblog/...
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/V*__*.sql
    └── test/java/com/eblog/...
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 应用入口 | `backend/src/main/java/com/eblog/EblogApplication.java` | `@EnableScheduling` 开启定时任务 |
| 安全策略（CSRF/JWT） | `backend/src/main/java/com/eblog/auth/SecurityConfig.java` | JWT Filter + 仅对写请求做 CSRF（部分端点例外） |
| 登录/注册/refresh/logout | `backend/src/main/java/com/eblog/auth/AuthController.java` | `/api/v1/auth/*`；refresh cookie 轮换逻辑在此 |
| 统一响应/错误码 | `backend/src/main/java/com/eblog/api/common` | `ApiResponse` + `ErrorCode` + `GlobalExceptionHandler` |
| 数据库迁移 | `backend/src/main/resources/db/migration` | Flyway 默认扫描位置 |
| MyBatis 扫描范围 | `backend/src/main/java/com/eblog/config/MybatisConfig.java` | `@MapperScan("com.eblog")`，Mapper 分散在各包 |

## CONVENTIONS
- **Auth**：`refresh_token`（HttpOnly+Strict）存库可撤销；access token 为 JWT（HMAC）。
- **UserId 注入方式**：业务层多处通过 `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` 解析 userId。
- **Moderation**：有 outbox + worker（定时任务）模式，发布后异步审核。

## ANTI-PATTERNS
- `backend/src/main/resources/application.yml` 当前包含敏感字段示例（JWT secret、DB password、MinIO key）。不要把真实值提交到仓库；生产建议用 profile/外部配置文件注入。

## COMMANDS
```bash
mvn -f backend/pom.xml test
mvn -f backend/pom.xml spring-boot:run
```
