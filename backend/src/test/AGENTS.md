# BACKEND TESTS (src/test)

## OVERVIEW
后端测试位于 `backend/src/test/java`，以 JUnit 为主，包含 Service/Controller/集成测试，以及可选的 Testcontainers 冒烟测试。

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 测试入口 | `backend/src/test/java/com/eblog/EblogApplicationTests.java` | Spring Boot 测试基座 |
| Auth 安全测试 | `backend/src/test/java/com/eblog/auth/*` | 包含 `SecurityConfigTest` 与鉴权相关覆盖 |
| Testcontainers 冒烟 | `backend/src/test/java/com/eblog/TestcontainersMysqlSmokeTest.java` | 需 `EBLOG_ENABLE_TESTCONTAINERS=true` 才会跑 |

## CONVENTIONS
- 测试默认应可在本地 DB/配置准备好时跑通；如要启用容器化数据库测试，按环境变量开关。

## COMMANDS
```bash
mvn -f backend/pom.xml test
```
