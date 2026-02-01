# BACKEND MAIN (src/main)

## OVERVIEW
`backend/src/main` 里是运行时源码与配置：Java 包 `com.eblog.*`、`application.yml`、Flyway 迁移脚本。

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 鉴权与 Token | `backend/src/main/java/com/eblog/auth` | JWT、refresh token 存库、CSRF 配置与过滤器 |
| 发文/阅读 | `backend/src/main/java/com/eblog/post` | `PostService` 控制格式：MARKDOWN/MDX；MDX 仅管理员 |
| 个人信息 | `backend/src/main/java/com/eblog/user/MeController.java` | `/api/v1/me` 与密码修改会撤销 refresh token |
| 评论/互动 | `backend/src/main/java/com/eblog/comment`、`backend/src/main/java/com/eblog/interaction` | 轻量 rate limit（内存 map + thread sleep） |
| 审核流水线 | `backend/src/main/java/com/eblog/moderation` | `WorkerService` 定时扫描 outbox；规则引擎 + 审核日志 |
| 上传（MinIO） | `backend/src/main/java/com/eblog/upload`、`backend/src/main/java/com/eblog/storage/MinioConfig.java` | 生成 presigned PUT URL |
| 配置与常量 | `backend/src/main/resources/application.yml` | `app.*` 自定义配置段，Flyway 也在此开启 |
| DB 迁移 | `backend/src/main/resources/db/migration` | 文件名 `V{N}__*.sql`；按版本递增 |

## CONVENTIONS
- **Flyway**：`spring.flyway.baseline-on-migrate: true`，本地已有库时会基线化。
- **MyBatis-Plus**：`map-underscore-to-camel-case: true`；Mapper 由 `@MapperScan("com.eblog")` 扫描。

## ANTI-PATTERNS
- 不要把 `backend/target/` 的生成物当成源码分析入口。
