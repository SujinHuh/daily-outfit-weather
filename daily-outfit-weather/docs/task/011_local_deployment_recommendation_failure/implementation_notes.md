# Implementation Notes: 011 Local Deployment Recommendation Failure

## Progress Log

- **2026-06-14:** Confirmed frontend recommendation loading calls `POST /api/recommendations/today`.
- **2026-06-14:** Confirmed `WeatherSnapshotProvider` supports default weather fallback, but only when `app.weather.fallback-enabled` is true.
- **2026-06-14:** Confirmed `docker-compose.prod.yml` currently forces `APP_WEATHER_FALLBACK_ENABLED: "false"`.
- **2026-06-14:** Ran targeted tests:
  - `./gradlew test --tests com.dailyoutfitweather.recommendation.WeatherSnapshotProviderTest --tests com.dailyoutfitweather.recommendation.RecommendationControllerIntegrationTest`
  - Result: passed.
- **2026-06-14:** Docker inspection showed backend container restarting.
- **2026-06-14:** Backend logs showed startup blocked by `ProductionConfigurationValidator` because `APP_FRONTEND_SUCCESS_URL` used localhost under the `prod` profile.

## Root Cause

The current local deployment stack mixes local URLs with strict production validation.

`docker-compose.prod.yml` defaults `SPRING_PROFILES_ACTIVE` to `prod`, while local smoke commonly uses `APP_FRONTEND_SUCCESS_URL=http://localhost:8080` or `http://localhost:5173`. The production validator rejects localhost, so the backend never becomes healthy.

The recommendation flow has an additional risk: `docker-compose.prod.yml` disables weather fallback. This contradicts the weather integration requirement that external API failure should not break today's recommendation.

## Recommended Fix

Use explicit deployment modes:

- Local production-like smoke: do not activate the strict `prod` validator, or use a dedicated smoke profile/compose file.
- Real production: keep `prod` active and use real public HTTPS origins.
- Weather fallback: make `APP_WEATHER_FALLBACK_ENABLED` environment-driven instead of hard-coded to false.

## Applied Changes

- **2026-06-14:** Changed `docker-compose.prod.yml` so `APP_WEATHER_FALLBACK_ENABLED` is environment-driven and defaults to `true`.
- **2026-06-14:** Added `.env.gcp.example` for a GCP public origin without committing the actual IP address.
- **2026-06-14:** The GCP env example maps frontend port `80`, sets OAuth redirect URI to `http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google`, and keeps `SESSION_COOKIE_SECURE=false` for plain HTTP endpoints.
- **2026-06-14:** Strengthened `ProductionConfigurationValidator` so `change-this...` and `example.com` placeholders cannot accidentally pass the `prod` profile.
- **2026-06-14:** Added `ProductionConfigurationValidatorTest` coverage for a public HTTP origin and placeholder rejection.
- **2026-06-14:** Ran a local deployment-like Docker smoke using compose project `dow-local-smoke`, frontend port `18080`, `SPRING_PROFILES_ACTIVE=local`, `SESSION_COOKIE_SECURE=false`, and `APP_WEATHER_FALLBACK_ENABLED=true`.
- **2026-06-14:** Docker build completed backend `bootJar`, built frontend image, started PostgreSQL/backend/frontend containers, and applied Flyway migrations.
- **2026-06-14:** Local deployment HTTP smoke passed for health and OAuth redirect. Additional checks confirmed frontend `/` returns 200, unauthenticated `/api/me` returns 401, OAuth starts with 302, and backend logs contain no recent `ERROR`, `Exception`, or `Restarting` pattern.
- **2026-06-14:** Full post-Google-login recommendation flow was not completed locally because it requires a real browser OAuth session and Google Console redirect registration for the tested callback URI.
- **2026-06-14:** Ran local `./gradlew bootJar` successfully and generated `backend/build/libs/daily-outfit-weather-backend-0.0.1-SNAPSHOT.jar`.
- **2026-06-14:** Investigated local Google login error `400 redirect_uri_mismatch`. The temporary `dow-local-smoke` backend had sent `GOOGLE_REDIRECT_URI=http://localhost:18080/login/oauth2/code/google`; this was only used to avoid an existing broken 8080 stack and is not the desired local verification baseline.
- **2026-06-14:** Recreated the primary local deployment stack on port `8080` with `SPRING_PROFILES_ACTIVE=local`, `APP_FRONTEND_SUCCESS_URL=http://localhost:8080`, `GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google`, `SESSION_COOKIE_SECURE=false`, and `APP_WEATHER_FALLBACK_ENABLED=true`.
- **2026-06-14:** 8080 local deployment smoke passed. Containers are `Up`, `/api/health` passes, and OAuth authorization redirects with `redirect_uri=http://localhost:8080/login/oauth2/code/google`.
