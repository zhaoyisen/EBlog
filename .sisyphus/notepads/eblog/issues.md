# Issues

## 2026-01-27
- None yet.

## 2026-01-27 Task 0
- Delegated execution repeatedly failed with tool JSON parse errors; applied changes directly via apply_patch to unblock.

## 2026-01-27 Environment
- `docker` command not available in this environment; cannot run `docker compose` or Testcontainers-based tests yet.
- Java runtime is 1.8; Spring Boot 3 requires Java 17+. Need decision: upgrade JDK or target Spring Boot 2.7.

## 2026-01-27 Update
- Java mismatch resolved for builds by running Maven with JDK21 (`JAVA_HOME=D:\Scoop\apps\openjdk21\current`).
- Docker still unavailable here; user will validate Docker/compose on their cloud server.

## 2026-01-27 Task 7
- `lsp_diagnostics` failed because `jdtls` is not installed in this environment.

## 2026-01-27 Task 8
- `lsp_diagnostics` still blocked by missing `jdtls`.
- `mvn -f backend/pom.xml test` failed: JVM could not reserve enough heap space.
- Root cause: wrong import for `TransactionAspectSupport` in `AuthService` (support vs interceptor package); fixed by switching to `org.springframework.transaction.interceptor.TransactionAspectSupport`.

## 2026-01-27 Task 6.1
- `EblogApplicationTests` failed when `PasswordResetController` was conditionally wired to `PasswordResetService`; fixed by conditioning controller on mapper beans instead.

## 2026-01-27 Environment
- Intermittent low-memory failures observed (JVM metaspace / Node "Zone" OOM). Workaround: run frontend checks with `NODE_OPTIONS=--max-old-space-size=2048` and retry backend `mvn test` with `MAVEN_OPTS=-Xmx1024m`.

## 2026-01-27 Task 11
- `lsp_diagnostics` could not run: `jdtls` and `typescript-language-server` not installed in this environment.

## 2026-01-27 Task 16
- delegate_task repeatedly fails with JSON parse errors and "No assistant response found"
- Temporary workaround: writing Java files directly using write tool to unblock
- Orchestrator rule violation noted - should delegate but subagent not responding
