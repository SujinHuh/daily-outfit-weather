# PR: Temporary GCP Login, Humidity Recommendations, and Tabbed Today UI

Branch:

```text
feature/gcp-temp-login-weather-tabs
```

PR creation URL:

```text
https://github.com/SujinHuh/daily-outfit-weather/pull/new/feature/gcp-temp-login-weather-tabs
```

## Summary

- Add temporary password login for GCP public-IP use while Google OAuth is blocked by public IP + HTTP constraints.
- Add configurable long-lived temporary-login session cookies for browser-close persistence.
- Parse KMA `REH` humidity and reflect humid heat in feels-like temperature and recommendation copy.
- Refresh stale saved recommendations when old weather JSON is missing humidity and returns `0`.
- Reorganize the today recommendation screen into tabs:
  - Today
  - Commute
  - Outfit
  - Items
  - Feedback
- Add notification click service worker handling and notification recommendation summary support.
- Add GCP temporary deployment docs, nginx proxy examples, and non-secret env examples.

## User-Facing Changes

- Users can open the app at `http://34.22.67.160/` and use temporary login with the configured password.
- Browser sessions can remain logged in after the browser window is closed when `SESSION_COOKIE_MAX_AGE` is set.
- Today recommendations now show humidity-aware wording such as humid heat guidance.
- Mobile users no longer need to scroll through a long detail page. Key detail areas are available through top tabs.
- Commute and leave-work weather are separated more clearly in the UI.

## Implementation Notes

- Temporary login:
  - `GET /api/auth-options`
  - `POST /api/temp-login`
  - Controlled by `APP_TEMP_LOGIN_ENABLED` and `APP_TEMP_LOGIN_PASSWORD`.
- Session persistence:
  - `SESSION_TIMEOUT`
  - `SESSION_COOKIE_MAX_AGE`
  - Current implementation persists browser cookies, but server-side sessions are still in-memory.
- Weather:
  - KMA `REH` is parsed into `WeatherSnapshot.humidity`.
  - High humidity increases feels-like temperature on hot days.
  - Saved recommendations with `humidity <= 0` are treated as stale and refreshed.
- Frontend:
  - Today dashboard now uses tab state for `today`, `commute`, `outfit`, `items`, and `feedback`.
  - Removed the scroll prompt.

## Validation

Backend:

```bash
./gradlew test --tests com.dailyoutfitweather.recommendation.RecommendationServiceTest
./gradlew test
```

Frontend:

```bash
npm run lint
npm run build
```

GCP smoke:

```text
GET  http://34.22.67.160/                         -> 200
GET  http://34.22.67.160/api/auth-options         -> {"tempLoginEnabled":true}
POST http://34.22.67.160/api/temp-login           -> 200
GET  http://34.22.67.160/api/recommendations/today -> humidity: 80
```

Session cookie verification:

```text
Set-Cookie: JSESSIONID=...; Max-Age=2592000; Expires=Mon, 31 Aug 2026 ...; HttpOnly; SameSite=Lax
```

## Deployment Notes

Current GCP temporary runtime uses:

```text
APP_TEMP_LOGIN_ENABLED=true
APP_TEMP_LOGIN_PASSWORD=<configured locally, not committed>
SESSION_TIMEOUT=30d
SESSION_COOKIE_MAX_AGE=30d
SESSION_COOKIE_SECURE=false
```

When a real domain and HTTPS are ready:

```text
APP_TEMP_LOGIN_ENABLED=false
SESSION_COOKIE_SECURE=true
GOOGLE_REDIRECT_URI=https://<domain>/login/oauth2/code/google
APP_FRONTEND_SUCCESS_URL=https://<domain>
APP_SECURITY_ALLOWED_ORIGINS=https://<domain>
```

## Risks and Follow-Ups

- Google OAuth still requires domain + HTTPS for real production usage.
- Temporary login should be disabled after OAuth is available.
- Server restarts can invalidate current in-memory sessions. Add Spring Session JDBC if restart-proof login persistence is required.
- Web Push automatic morning notifications are still a follow-up. The current notification behavior is manual/test-oriented.
- PWA cache may require a hard refresh or app restart to show the newest tab UI after deployment.
- `codex-test.txt` is intentionally excluded from this PR.
