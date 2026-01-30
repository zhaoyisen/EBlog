# enhance-frontend-features - issues
# Append-only. New entries go at bottom.

## 2026-01-27 Init
- (empty)

## 2026-01-27 Backend API Quirks
- Backend runs with `server.servlet.context-path: /api/v1` AND controllers also prefix `/api/v1/...`, resulting in effective routes like `/api/v1/api/v1/...`.
- `POST /api/v1/api/v1/auth/login` returns `data.accessToken` and sets refresh token as an HttpOnly cookie; it does not return `refresh_token` in JSON.
