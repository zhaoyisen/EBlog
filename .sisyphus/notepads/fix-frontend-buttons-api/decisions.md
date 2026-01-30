# Decisions - Fix Frontend Buttons and API

*Last Updated: 2026-01-27*

## Task 1: Create .env.local
- Decision: Use `.env.local` for local development (not `.env`)
- Reason: `.env.local` overrides other `.env` files and is gitignored by default in Next.js

## Task 2: Fix Home Page Links
- Decision: Keep `/login` link even if page doesn't exist yet
- Reason: Placeholder for future login page implementation

## Task 3: Docker Compose Environment Variable
- Decision: Change `NEXT_PUBLIC_API_URL` to `NEXT_PUBLIC_API_BASE`
- Reason: Match frontend code expectation, remove `/api/v1` suffix

## Task 4: Backend Startup
- Decision: Provide both Docker and standalone options
- Reason: User may not have Docker available

## Pending Decisions
- [ ] Any future decisions will be recorded here
