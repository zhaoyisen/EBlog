# Learnings - Fix Frontend Buttons and API

*Last Updated: 2026-01-27*

## Conventions Discovered
- [x] Document any code patterns found during fixes
- [x] Record best practices for Next.js environment variables
- [x] Note any frontend routing patterns

## Task 1 Learnings (2026-01-27)
- Created `.env.local` file for local development environment variables
- Next.js automatically loads `.env.local` in development mode
- Environment variables must start with `NEXT_PUBLIC_` to be exposed to browser
- `.env.local` overrides other `.env` files, making it ideal for local development
- File path must be correct: `frontend/.env.local`, not project root

## Task 2 Learnings (2026-01-27)
- Fixed 5 navigation links from `href="#"` to actual routes
- Next.js uses file-based routing with `/` as root
- Routes: `/posts`, `/authors`, `/login` map to corresponding `app/*/page.tsx` files
- Keeping `/login` link even if page doesn't exist provides placeholder for future implementation
- Navigation structure now matches Next.js routing conventions

## Task 3 Learnings (2026-01-27)
- Changed Docker Compose env variable from `NEXT_PUBLIC_API_URL` to `NEXT_PUBLIC_API_BASE`
- Removed `/api/v1` suffix from Docker env variable
- Frontend code already includes `/api/v1` in apiUrl() function
- Docker env variable now matches frontend code expectations
- Environment variable consistency between Docker and code is critical for functionality

## Task 4 Learnings (2026-01-27)
- Docker not available on Windows development machine
- MySQL already running on localhost:3306
- MinIO not running (would need manual start or Docker)
- Spring Boot started successfully via `mvn spring-boot:run`
- Backend running on port 8080 with context path `/api/v1`
- Database migrations applied successfully (14 migrations validated)
- HikariPool connection established to MySQL
- Startup time: ~3 seconds
- Health endpoint may not be implemented, but backend is operational

## Patterns Observed
- [x] Environment variable naming conventions
- [x] Docker Compose environment variable structure
- [x] Next.js routing patterns

## Gotchas Found
- [ ] Any surprising behaviors discovered
- [ ] Common pitfalls to avoid

## Future Improvements
- [ ] Ideas for better error handling
- [ ] Suggestions for better DX
