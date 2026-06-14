# Issue: 011 Local Deployment Recommendation Failure

## Problem

After deployment-oriented configuration changes, the local deployed stack no longer completes the login -> profile/location setup -> today recommendation flow.

Observed user-facing messages:

- `요청을 처리하지 못했습니다.`
- `오늘 추천을 준비하고 있습니다.`

## Context

- The user can reach the frontend and has completed Google login.
- Recommendation generation is triggered after location/profile setup.
- The project is currently using a partial Harness Kit overlay as described in `docs/harness-kit-notes.md`.

## Evidence Collected

- `docker ps` showed `daily-outfit-weather-backend-1` in `Restarting (1)` state.
- Backend container logs showed startup failure:
  - `IllegalStateException: APP_FRONTEND_SUCCESS_URL must be configured for prod profile`
  - The value was rejected because the `prod` validator disallows localhost values.
- `docker-compose.prod.yml` defaults `SPRING_PROFILES_ACTIVE` to `prod`.
- `.env.example` uses localhost-oriented values such as `APP_FRONTEND_SUCCESS_URL=http://localhost:5173`.
- `docker-compose.prod.yml` also hard-codes `APP_WEATHER_FALLBACK_ENABLED: "false"`, which conflicts with the weather integration requirement that external API failures should not break today's recommendation.

## Initial Diagnosis

There are two deployment configuration regressions:

1. Local smoke runs through `docker-compose.prod.yml`, but the backend starts with the real `prod` profile and rejects localhost configuration before serving requests.
2. Even after startup is fixed, recommendation generation can fail if KMA is unavailable because production compose disables weather fallback.

## Non-Goals

- Do not weaken real production validation silently.
- Do not remove Google OAuth, CSRF, or production cookie protections.
- Do not introduce a live KMA integration test that depends on external network availability.
