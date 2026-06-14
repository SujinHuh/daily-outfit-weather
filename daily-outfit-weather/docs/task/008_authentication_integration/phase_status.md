# Phase Status: 008 Authentication Integration

## Current State

- Task Status: `debugging_csrf`
- Current Phase: `Phase 7`
- Current Gate: `ready for re-testing onboarding`
- Last Approved Phase: `Phase 6`

## Allowed Write Set

- `docs/task/008_authentication_integration/**`
- `backend/build.gradle`
- `backend/src/main/java/com/dailyoutfitweather/global/config/SecurityConfig.java`
- `backend/src/main/java/com/dailyoutfitweather/global/config/WebConfig.java`
- `backend/src/main/java/com/dailyoutfitweather/global/security/**`
- `backend/src/main/java/com/dailyoutfitweather/user/**`
- `backend/src/main/java/com/dailyoutfitweather/profile/controller/ProfileController.java`
- `backend/src/main/java/com/dailyoutfitweather/recommendation/controller/RecommendationController.java`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/**`
- `frontend/vite.config.ts`
- `frontend/src/App.tsx`
- `.env.example`

## Locked Paths

- `docs/project-brief.md`
- `docs/review-notes.md`

## Completed Follow-up

- Vite dev proxy now forwards `/oauth2/**` and `/login/**` to the backend.
- OAuth login success URL is configurable via `APP_FRONTEND_SUCCESS_URL`.
- `@LoginUser` resolution failure now returns 401 instead of passing `null` into controllers.
- Existing email accounts can be linked to the Google provider to avoid email unique constraint failures.
- Frontend lint/build and backend full test suite pass after the authentication fixes.

## Next Action

- Run a real Google OAuth2 browser smoke test with valid `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and Google Console redirect URI.
- Decide the production CSRF/CORS/cookie policy before external deployment.
