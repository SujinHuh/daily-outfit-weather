# Validation Report: 011 Local Deployment Recommendation Failure

## Automated Validation

- Passed:
  - `./gradlew test --tests com.dailyoutfitweather.recommendation.WeatherSnapshotProviderTest --tests com.dailyoutfitweather.recommendation.RecommendationControllerIntegrationTest`
  - `./gradlew test --tests com.dailyoutfitweather.global.config.ProductionConfigurationValidatorTest --tests com.dailyoutfitweather.recommendation.WeatherSnapshotProviderTest --tests com.dailyoutfitweather.recommendation.RecommendationControllerIntegrationTest`
  - `./gradlew bootJar`
  - `docker compose --env-file .env.gcp.example -f docker-compose.prod.yml config`

## Runtime Validation

- Docker container state was checked after permission escalation.
- Backend container was not serving traffic; it was restarting.
- Backend logs confirmed startup failure in `ProductionConfigurationValidator`.
- Local deployment-like stack was rebuilt and started with:
  - compose project: `dow-local-smoke`
  - frontend port: `18080`
  - profile: `local`
  - weather fallback: `true`
- Passed:
  - Local JAR artifact was generated at `backend/build/libs/daily-outfit-weather-backend-0.0.1-SNAPSHOT.jar`.
  - Backend Docker build ran `./gradlew bootJar --no-daemon` successfully.
  - Frontend Docker image built successfully.
  - PostgreSQL/backend/frontend containers reached `Up`; PostgreSQL was `healthy`.
  - Backend started with `local` profile and applied Flyway migrations through version 3.
  - `BASE_URL=http://127.0.0.1:18080 scripts/deployment-smoke.sh` passed.
  - `GET /` returned 200 through nginx.
  - `GET /api/me` returned 401 before login, as expected.
  - `GET /oauth2/authorization/google` returned 302 with `redirect_uri=http://localhost:18080/login/oauth2/code/google`.
  - Backend recent logs contained no `ERROR`, `Exception`, or `Restarting` pattern.

## Not Yet Executed

- Full local browser login -> profile/location setup -> recommendation click, because the in-app browser was unavailable in this session and Google OAuth requires a registered callback URI for `http://localhost:18080/login/oauth2/code/google`.
- Full browser OAuth -> onboarding/profile -> recommendation smoke on the GCP public endpoint.
- Rebuilt GCP Docker stack with real secret values.

## Residual Risk

- The Google Console must include `http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google` as an authorized redirect URI.
- To repeat the local full-login smoke on port `18080`, Google Console must also include `http://localhost:18080/login/oauth2/code/google`, or the smoke must use a port/redirect URI that is already registered.
- The `.env.gcp.example` placeholder secrets must be replaced before GCP deployment.
- The current GCP origin is HTTP. When HTTPS/domain is added, `APP_FRONTEND_SUCCESS_URL`, `APP_SECURITY_ALLOWED_ORIGINS`, `GOOGLE_REDIRECT_URI`, and `SESSION_COOKIE_SECURE` must be updated together.

## Latest Local OAuth Finding

- Browser login failed with Google `400 redirect_uri_mismatch`.
- The temporary `18080` smoke used a callback URI that was not the intended baseline.
- The primary local deployment stack has been recreated on port `8080`.
- Active 8080 local backend env:
  - `APP_FRONTEND_SUCCESS_URL=http://localhost:8080`
  - `GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google`
- Confirmed `/oauth2/authorization/google` sends `redirect_uri=http://localhost:8080/login/oauth2/code/google`.
- Root cause: the Google OAuth client must have that exact redirect URI registered.
