# Deployment Readiness Log

## 2026-06-14 Local Production Smoke Fixes

### Scope

운영 준비 1번 항목인 로컬 운영형 실행 문제를 점검하면서 production compose smoke와 브라우저 로그인 시작 흐름에서 발견된 문제를 수정했다.

### Changes

- Backend log volume permission
  - backend image에서 `/app/logs`를 미리 생성하고 `app:app` 소유로 설정했다.
  - production compose의 named volume이 붙어도 `LOG_FILE=/app/logs/backend.log` 파일을 `app` 사용자가 생성할 수 있게 했다.

- OAuth start route through frontend/nginx
  - nginx proxy가 backend로 전달하는 `Host`를 `$host` 대신 `$http_host`로 바꿔 외부 포트까지 보존하게 했다.
  - `X-Forwarded-Host`도 함께 전달한다.
  - 로컬 production smoke의 OAuth redirect URI가 `http://localhost/login/oauth2/code/google`가 아니라 `http://localhost:18080/login/oauth2/code/google`로 생성되는 것을 확인했다.

- PWA navigation fallback
  - service worker navigation fallback에서 `/api/`, `/oauth2/`, `/login/`을 제외했다.
  - 브라우저가 `/oauth2/authorization/google`를 React 앱 경로로 오인해 로그인 화면으로 이동하지 못하는 문제를 막는다.

### Validation Commands

```bash
cd frontend
npm run lint
npm run build

cd ..
FRONTEND_PORT=18080 \
APP_FRONTEND_SUCCESS_URL=https://daily-outfit-weather.example.com \
APP_SECURITY_ALLOWED_ORIGINS=https://daily-outfit-weather.example.com \
APP_NOTIFICATION_GENERATE_DUE_TOKEN=prod-smoke-token \
KMA_SERVICE_KEY=prod-smoke-kma-key \
GOOGLE_CLIENT_ID=prod-smoke-google-client-id \
GOOGLE_CLIENT_SECRET=prod-smoke-google-client-secret \
docker compose -p dow-prod-smoke --env-file .env -f docker-compose.prod.yml up -d --build --force-recreate

BASE_URL=http://localhost:18080 scripts/deployment-smoke.sh
curl -i -s http://localhost:18080/oauth2/authorization/google
curl -fsS http://localhost:18080/sw.js
docker compose -p dow-prod-smoke -f docker-compose.prod.yml exec -T backend ls -l /app/logs
```

### Result

- `npm run lint` 통과
- `npm run build` 통과
- production compose 재빌드 및 기동 성공
- `scripts/deployment-smoke.sh` 통과
- `/app/logs/backend.log`가 `app app` 소유로 생성됨
- backend 로그의 `/app/logs/backend.log (Permission denied)` 오류가 사라짐
- OAuth 시작 응답이 Google authorization URL로 302를 반환하고, `redirect_uri`에 `localhost:18080` 포트가 포함됨
- 생성된 `sw.js`의 navigation fallback denylist에 `/api/`, `/oauth2/`, `/login/`이 포함됨
- 브라우저에서 `invalid_client`가 발생한 것은 local smoke stack을 dummy Google client 값으로 띄웠기 때문임을 확인했다.
- stack을 `.env`의 Google client 값으로 재기동했고, OAuth 시작 응답에서 dummy client 값이 제거된 것을 확인했다.

### Caveat

현재 local production smoke는 Google authorization URL 생성까지만 검증했다. 실제 OAuth full-flow 검증은 4번 항목에서 Google Console redirect URI와 실제 client secret으로 별도 진행한다. 로컬 production smoke에서 다음 단계로 넘어가려면 Google Console에 `http://localhost:18080/login/oauth2/code/google`을 Authorized redirect URI로 등록해야 한다.

### 2026-06-14 Redirect URI Mismatch Investigation

브라우저에서 Google OAuth 진입 후 `400 redirect_uri_mismatch`가 발생했다. 서브에이전트와 메인 세션에서 설정과 실제 응답을 대조했다.

확인된 현재 local production compose OAuth 시작 응답:

```text
client_id=<configured Google client id>
redirect_uri=http://localhost:18080/login/oauth2/code/google
```

원인:

- 현재 접속 주소가 `http://localhost:18080`이므로 Spring Security가 Google에 전달하는 callback URI도 `http://localhost:18080/login/oauth2/code/google`이다.
- 예전에 로컬 dev server로 테스트했다면 callback URI는 `http://localhost:5173/login/oauth2/code/google`였을 가능성이 높다.
- Google Console의 Authorized redirect URI는 scheme, host, port, path가 모두 정확히 일치해야 하므로 `localhost:5173` 또는 `localhost:8080`만 등록되어 있으면 `localhost:18080` smoke에서는 `redirect_uri_mismatch`가 발생한다.

Google Console에 등록해야 하는 로컬 테스트 URI:

```text
http://localhost:18080/login/oauth2/code/google
http://localhost:5173/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/google
```

운영 배포 시에는 다음 형태를 추가 등록해야 한다.

```text
https://<production-host>/login/oauth2/code/google
```

Follow-up:

- 기존 로컬 테스트에서 사용했을 가능성이 높은 `localhost:5173`에 맞춰 production smoke stack을 `FRONTEND_PORT=5173`으로 재기동했다.
- `BASE_URL=http://localhost:5173 scripts/deployment-smoke.sh` 통과를 확인했다.
- OAuth 시작 응답의 callback URI가 `http://localhost:5173/login/oauth2/code/google`로 생성되는 것을 확인했다.
- 추가 서브에이전트 재조사에서도 Google 자체 `400 redirect_uri_mismatch` 화면까지 도달한 경우는 앱 라우팅 문제가 아니라 활성 OAuth client의 Authorized redirect URI 등록 불일치로 판단했다.
- 현재 runtime `client_id`가 가리키는 OAuth client에 `Authorized JavaScript origins=http://localhost:5173`와 `Authorized redirect URIs=http://localhost:5173/login/oauth2/code/google`가 정확히 등록되어야 한다.

### 2026-06-14 Local OAuth Callback Error Follow-up

Google Console redirect URI 등록 후 callback이 `/login?error`로 떨어지는 현상을 확인했다.

원인:

- production compose 기본값은 `SESSION_COOKIE_SECURE=true`이다.
- 로컬 테스트 주소는 `http://localhost:5173`이므로 브라우저가 `Secure` 속성이 붙은 `JSESSIONID`를 OAuth callback 요청에 보내지 않는다.
- Spring Security OAuth state가 세션에 저장되어 있으므로 callback에서 세션을 찾지 못하면 `/login?error`로 이동한다.

조치:

- `docker-compose.prod.yml`의 `SPRING_PROFILES_ACTIVE`를 `${SPRING_PROFILES_ACTIVE:-prod}`로 바꿔 기본 prod 동작은 유지하면서 local OAuth smoke override가 가능하게 했다.
- local OAuth smoke stack은 다음 값으로 재기동했다.

```text
SPRING_PROFILES_ACTIVE=local
FRONTEND_PORT=5173
APP_FRONTEND_SUCCESS_URL=http://localhost:5173
APP_SECURITY_ALLOWED_ORIGINS=http://localhost:5173
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=lax
```

검증:

- backend runtime env에서 `SPRING_PROFILES_ACTIVE=local`, `SESSION_COOKIE_SECURE=false`, `APP_FRONTEND_SUCCESS_URL=http://localhost:5173` 확인
- OAuth 시작 응답의 `JSESSIONID`에서 `Secure` 속성이 제거된 것을 확인
- OAuth 시작 응답의 `redirect_uri=http://localhost:5173/login/oauth2/code/google` 확인
- `BASE_URL=http://localhost:5173 scripts/deployment-smoke.sh` 통과

### 2026-06-14 Explicit OAuth Redirect URI

기존에 로그인됐던 환경과 현재 production-like compose 환경의 callback URI 차이를 줄이기 위해 OAuth redirect URI를 환경변수로 명시 설정 가능하게 했다.

변경:

- `backend/src/main/resources/application.yml`
  - `spring.security.oauth2.client.registration.google.redirect-uri=${GOOGLE_REDIRECT_URI:{baseUrl}/login/oauth2/code/{registrationId}}` 추가
- `docker-compose.prod.yml`
  - `GOOGLE_REDIRECT_URI=${GOOGLE_REDIRECT_URI:-${APP_FRONTEND_SUCCESS_URL}/login/oauth2/code/google}` 전달
- `.env.example`
  - 로컬 기본값 `GOOGLE_REDIRECT_URI=http://localhost:5173/login/oauth2/code/google` 문서화

검증 중 발견 및 수정:

- Docker Compose 기본값에 Spring placeholder인 `{baseUrl}`를 직접 넣으면 Compose interpolation 때문에 URI가 깨질 수 있음을 확인했다.
- compose 기본값을 `APP_FRONTEND_SUCCESS_URL` 기반의 명시 URI로 바꿔 문제를 제거했다.

현재 local OAuth smoke runtime 값:

```text
GOOGLE_REDIRECT_URI=http://localhost:5173/login/oauth2/code/google
APP_FRONTEND_SUCCESS_URL=http://localhost:5173
SESSION_COOKIE_SECURE=false
SPRING_PROFILES_ACTIVE=local
```

현재 OAuth 시작 응답:

```text
client_id=<configured Google client id>
redirect_uri=http://localhost:5173/login/oauth2/code/google
```

`BASE_URL=http://localhost:5173 scripts/deployment-smoke.sh` 통과.

### 2026-06-14 OAuth Port Correction

사용자 확인 과정에서 기존에 Google OAuth 로그인이 성공하던 로컬 주소는 `localhost:8080` 기준이었을 가능성이 높음을 확인했다.

문제:

- 기존 Google Console에는 `http://localhost:8080/login/oauth2/code/google`가 등록되어 있었던 것으로 보인다.
- 점검 중 production-like stack을 `FRONTEND_PORT=5173`으로 띄우면서 앱이 Google에 `http://localhost:5173/login/oauth2/code/google`를 보내게 했다.
- Google OAuth redirect URI는 scheme, host, port, path가 모두 정확히 일치해야 하므로, Console에 `8080`만 등록된 상태에서 `5173` 요청을 보내면 `400 redirect_uri_mismatch`가 발생한다.

정정:

- local OAuth smoke stack을 `localhost:8080` 기준으로 재기동했다.

```text
FRONTEND_PORT=8080
APP_FRONTEND_SUCCESS_URL=http://localhost:8080
APP_SECURITY_ALLOWED_ORIGINS=http://localhost:8080
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
SESSION_COOKIE_SECURE=false
SPRING_PROFILES_ACTIVE=local
```

검증:

```text
BASE_URL=http://localhost:8080 scripts/deployment-smoke.sh
```

결과:

- smoke 통과
- OAuth 시작 응답의 `redirect_uri=http://localhost:8080/login/oauth2/code/google` 확인

교훈:

- OAuth smoke는 기존 Google Console에 등록된 redirect URI 기준 포트를 먼저 확인한 뒤 시작해야 한다.
- 포트를 임의로 `5173`, `18080` 등으로 바꾸면 Google Console 등록값과 불일치해 로그인 시작이 차단된다.

## 2026-06-14 Location Search Coverage Finding

### Scope

사용자 확인 중 위치 검색이 일부 동네만 지원되는 문제가 지적되었다. 예시는 `성내동`, `잠실`이지만, 요구사항은 특정 샘플 지역이 아니라 사용자가 입력하는 모든 동네 검색을 지원하는 것이다.

### Current Finding

- 현재 `backend/src/main/resources/location/kma_location_grids.csv`는 헤더 포함 20줄뿐이다.
- 실제 지원 위치는 샘플/allowlist 19개에 불과하다.
- 따라서 `성내`, `잠실`, `판교` 같은 일부 지역은 검색되지만, 전국 또는 서울 전체 동네 검색을 지원한다고 볼 수 없다.

### Immediate Fix

- `성내동`처럼 사용자가 기본 동 이름으로 입력해도 `성내1동`, `성내2동`, `성내3동`이 검색되도록 행정동 alias matching을 추가했다.
- `잠실동` 입력 시 `잠실본동`, `잠실2동`, `잠실3동`, `잠실4동`, `잠실6동`, `잠실7동`이 검색되도록 테스트를 추가했다.

검증:

```text
cd backend
./gradlew test --tests com.dailyoutfitweather.location.LocationGridCatalogTest
```

결과:

- 테스트 통과

### Nationwide Catalog Follow-up

- 샘플 CSV를 전국 행정동/읍면동 생성 catalog로 교체했다.
- `scripts/generate-kma-location-grids.mjs`를 추가해 행정동 GeoJSON 중심 좌표를 KMA DFS `nx`, `ny`로 변환한다.
- `backend/src/main/resources/location/kma_location_grids.csv`는 헤더 포함 3,496줄, 실제 위치 3,495개로 확장되었다.
- `해운대`, `서귀포`, `조치원`, `성내동`, `잠실동` 검색 케이스를 테스트에 포함했다.
- backend 전체 테스트와 localhost smoke를 통과했다.

남은 검증 리스크:

- 현재 좌표는 행정동 polygon centroid 기반 생성값이다.
- 공식 KMA per-dong 대표 격자표가 확보되면 주요 지역과 대조 검증해야 한다.
- GeoJSON 원천 저장소에 명시 라이선스가 없으므로 운영 출시 전 데이터 출처/라이선스 검토 또는 승인된 데이터 원천 대체가 필요하다.

## 2026-06-07 Production Hardening

### Scope

서브에이전트 배포 준비 검토에서 나온 1~5번 리스크를 운영 배포 준비 항목으로 보강했다.

### Changes

- CSRF
  - 세션 쿠키 기반 OAuth 인증에 맞춰 CSRF 보호를 활성화했다.
  - `XSRF-TOKEN` 쿠키와 `X-XSRF-TOKEN` 헤더 방식을 사용한다.
  - 프론트엔드 요청 wrapper가 mutating 요청에 CSRF 헤더를 자동 포함한다.
  - 내부 배치용 `POST /api/notifications/generate-due`는 CSRF 대신 `X-Internal-Job-Token`으로 보호한다.

- CORS, cookie, OAuth production settings
  - `APP_SECURITY_ALLOWED_ORIGINS`로 허용 origin을 환경별로 주입한다.
  - 세션 쿠키 `HttpOnly`, `Secure`, `SameSite` 설정을 환경변수로 제어한다.
  - `server.forward-headers-strategy=framework`로 프록시 뒤 OAuth redirect와 secure cookie 판단을 보조한다.
  - `prod` profile에서 OAuth/KMA/internal token/frontend URL이 누락되거나 localhost/dev placeholder면 부팅 실패한다.

- Deployment topology
  - `backend/Dockerfile`을 추가했다.
  - `frontend/Dockerfile`과 `frontend/nginx.conf`를 추가했다.
  - `docker-compose.prod.yml`을 추가해 PostgreSQL, backend, frontend/nginx same-origin 배포 구성을 정의했다.
  - Nginx는 정적 프론트를 서빙하고 `/api`, `/oauth2`, `/login`을 backend로 프록시한다.
  - 운영 도메인/TLS/OAuth redirect URI 준비 절차는 [Security Deployment Policy](./security-deployment-policy.md)의 `Domain and TLS` 및 `Google OAuth Smoke Test` 기준을 따른다.

- Weather and location correctness
  - `APP_WEATHER_FALLBACK_ENABLED=false`일 때 KMA API 실패 또는 KMA grid 누락을 기본 날씨로 덮지 않고 `503 WEATHER_UNAVAILABLE`로 반환한다.
  - 운영 compose는 weather fallback을 비활성화한다.
  - 위치 선택은 현재 catalog에 있는 지역만 검색/선택하는 흐름을 유지한다.
  - 현재 지원 위치와 catalog 확장 절차는 `docs/kma-service-area.md`에 명시했다.

### Remaining Caveats

- 실제 운영 도메인 배포 전 public origin을 `https://<운영 host>` 형태로 확정하고 DNS/TLS를 먼저 검증해야 한다.
- Google Console에는 운영 redirect URI `https://<운영 host>/login/oauth2/code/google`을 등록해야 한다.
- 운영 플랫폼이 TLS를 종료한다면 proxy가 `Host`와 `X-Forwarded-Proto: https`를 backend까지 보존해야 한다.
- KMA location grid catalog는 아직 전국 전체 데이터가 아니므로, `docs/kma-service-area.md`의 지원 범위로 서비스를 제한하거나 catalog 확장이 필요하다.
- 로그는 컨테이너 볼륨에 남도록 구성했지만, 운영 로그 수집/보관/알림 시스템은 별도 인프라에서 연결해야 한다. 운영 절차는 [Operations Logs and Monitoring Runbook](./operations-logs-monitoring-runbook.md)을 따른다.
- 운영 DB 백업/복구/마이그레이션 롤백 절차는 [Operations DB Runbook](./operations-db-runbook.md)에 정리했다. 실제 운영 플랫폼의 백업 저장소와 알림 연결은 별도 인프라 작업으로 남는다.

### Validation Commands

```bash
cd backend
./gradlew test --rerun-tasks

cd ../frontend
npm run lint
npm run build

cd ..
docker compose --env-file .env -f docker-compose.prod.yml config --services
docker compose --env-file .env -f docker-compose.prod.yml build backend frontend

sh -n scripts/deployment-smoke.sh
sh -n scripts/db-backup.sh
sh -n scripts/db-restore.sh
```

추가 검증:

- 부적절한 `APP_NOTIFICATION_GENERATE_DUE_TOKEN` 값에서는 `prod` profile 부팅이 실패하는 것을 확인했다.
- 운영형 URL과 internal token override 후 `prod` profile 백엔드가 정상 부팅하는 것을 확인했다.
- `GET /api/health`가 200을 반환하고 `XSRF-TOKEN` 쿠키를 발급하는 것을 확인했다.
- `scripts/deployment-smoke.sh`, `scripts/db-backup.sh`, `scripts/db-restore.sh` 문법 검사를 통과했다.
- `.env`와 로컬 PostgreSQL 컨테이너 기준 `scripts/db-backup.sh`가 백업 파일을 생성하는 것을 확인했다.
- `scripts/db-backup.sh`와 `scripts/db-restore.sh`는 `COMPOSE_FILE`이 설정되면 prod compose service name `postgres`를 사용하고, 설정되지 않으면 기존 로컬 개발 컨테이너명을 사용한다.

### Production Smoke Checklist

환경 준비:

- DNS가 `https://<운영 host>`를 실제 배포 entrypoint로 연결한다.
- TLS 인증서가 `https://<운영 host>`에서 브라우저 오류 없이 유효하다.
- `.env` 또는 secret store에 다음 운영값이 설정되어 있다.
  - `APP_FRONTEND_SUCCESS_URL=https://<운영 host>`
  - `APP_SECURITY_ALLOWED_ORIGINS=https://<운영 host>`
  - `SESSION_COOKIE_SECURE=true`
  - `SESSION_COOKIE_SAME_SITE=lax`
- Google Console OAuth client에 다음 값이 등록되어 있다.
  - Authorized JavaScript origin: `https://<운영 host>`
  - Authorized redirect URI: `https://<운영 host>/login/oauth2/code/google`

컨테이너 smoke:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up --build
curl -i http://localhost:${FRONTEND_PORT:-8080}/api/health
curl -I http://localhost:${FRONTEND_PORT:-8080}/oauth2/authorization/google
```

운영 도메인 smoke:

```bash
curl -i https://<운영 host>/api/health
curl -I https://<운영 host>/oauth2/authorization/google
```

브라우저에서 확인할 항목:

- Google 로그인 화면으로 이동한다.
- 로그인 완료 후 `APP_FRONTEND_SUCCESS_URL`로 돌아온다.
- `GET /api/me`가 로그인 사용자를 반환한다.
- 온보딩 저장, 오늘 추천 생성, 피드백 저장 요청이 CSRF 오류 없이 동작한다.
- 새로고침 후에도 로그인 세션이 유지된다.
- 로그아웃 후 보호 API가 401을 반환한다.
- KMA 장애 또는 미지원 위치에서 운영 fallback 비활성화 시 `WEATHER_UNAVAILABLE`이 반환된다.

## 2026-06-07 External Readiness Recheck

### Scope

서브에이전트로 운영 전 잔여 항목을 재검토하고, 로컬에서 가능한 production compose smoke를 추가 실행했다.

### Confirmed In Repo

- 운영 도메인/TLS/proxy header 기준은 [Security Deployment Policy](./security-deployment-policy.md)에 정리되어 있다.
- Google Console OAuth origin/redirect URI 등록 절차와 브라우저 full-flow smoke checklist가 문서화되어 있다.
- `docker-compose.prod.yml`은 `postgres`, `backend`, `frontend` 서비스를 정상 파싱한다.
- `scripts/deployment-smoke.sh`는 health와 OAuth redirect smoke를 자동화한다.
- `scripts/db-backup.sh`와 `scripts/db-restore.sh`는 prod compose service name과 기존 로컬 컨테이너명 양쪽을 지원한다.
- DB backup/restore/migration rollback 절차와 logs/monitoring/incident triage runbook이 추가되어 있다.
- KMA 서비스 지역 제한과 확장 gate가 [KMA Location Grid Service Area](./kma-service-area.md)에 정리되어 있다.

### Additional Local Smoke

```bash
FRONTEND_PORT=18080 \
APP_FRONTEND_SUCCESS_URL=https://daily-outfit-weather.example.com \
APP_SECURITY_ALLOWED_ORIGINS=https://daily-outfit-weather.example.com \
APP_NOTIFICATION_GENERATE_DUE_TOKEN=prod-smoke-token \
docker compose -p dow-prod-smoke --env-file .env -f docker-compose.prod.yml up -d --build

BASE_URL=http://localhost:18080 scripts/deployment-smoke.sh

FRONTEND_PORT=18080 \
APP_FRONTEND_SUCCESS_URL=https://daily-outfit-weather.example.com \
APP_SECURITY_ALLOWED_ORIGINS=https://daily-outfit-weather.example.com \
APP_NOTIFICATION_GENERATE_DUE_TOKEN=prod-smoke-token \
docker compose -p dow-prod-smoke --env-file .env -f docker-compose.prod.yml down -v
```

결과:

- 별도 compose project `dow-prod-smoke`로 production stack이 기동했다.
- `GET /api/health` smoke가 통과했다.
- `/oauth2/authorization/google` 302 redirect smoke가 통과했다.
- smoke용 컨테이너와 볼륨은 정리했다.

### Still External

- 실제 public domain, DNS, TLS certificate, production host/load balancer/proxy provisioning
- Google Console 운영 OAuth client 등록과 consent/test user 설정
- 실제 운영 도메인에서 브라우저 Google OAuth login/callback/session/CSRF/logout full-flow smoke
- 서비스 지역 launch decision: 현재 KMA catalog 지원 지역으로 제한하거나 catalog를 확장
- 운영 백업 저장소, restore drill scheduling, log shipping, retention, alerting system 연결

## 2026-06-14 Local OAuth Onboarding Failure

### Symptom

로컬 production smoke 환경(`http://localhost:8080`)에서 Google OAuth 로그인은 완료됐지만, 온보딩 입력 완료 후 화면에 `요청을 처리하지 못했습니다.`가 표시됐다.

### Evidence

- `/oauth2/authorization/google` returned `302`.
- `/login/oauth2/code/google?...` returned `302`.
- `/api/me` returned `200`, confirming the Google login session was established.
- `/api/profile` returned `404`, which is expected before first onboarding.
- Location search requests such as `성내동` and `잠실` returned `200`.
- `POST /api/profile/onboarding` returned `403`.

### Cause

The failure was not an OAuth redirect issue and not a location catalog issue. The onboarding save request was blocked by Spring Security CSRF protection because:
1. The frontend had no fresh `XSRF-TOKEN` cookie/header pair before the first mutating request after login.
2. Spring Security 6 default `XorCsrfTokenRequestAttributeHandler` expects XORed tokens in headers, but the frontend was sending the raw token from the cookie.

### Fix

- Updated `SecurityConfig.java` to use `CsrfTokenRequestAttributeHandler` with `csrfRequestAttributeName(null)` to support raw tokens in SPA headers.
- Updated the frontend API request helper to call `GET /api/health` before mutating requests when the `XSRF-TOKEN` cookie is missing.
- The request helper now reads the refreshed `XSRF-TOKEN` cookie and sends it as `X-XSRF-TOKEN` on POST/PUT/PATCH/DELETE requests.
- Added console logging for 403 errors in the frontend to aid future debugging.
- Rebuilt the local production Docker stack and confirmed the shipped frontend bundle contains the CSRF bootstrap logic.

### Verification

- `npm run lint` passed.
- `npm run build` passed.
- `BASE_URL=http://localhost:8080 scripts/deployment-smoke.sh` passed.
- Container logs confirmed the prior user-facing failure matched `POST /api/profile/onboarding` `403`.

### Retest Required

브라우저에서 강력 새로고침 후 다시 온보딩 저장을 눌러야 한다. 기존 service worker 또는 캐시가 이전 JS 번들을 잡고 있으면 같은 증상이 반복될 수 있다.

## 2026-06-14 GCP Endpoint Deployment Recommendation Fix

### Symptom

GCP VM public endpoint를 운영 배포 주소로 쓰려는 상태에서 추천 요청이 `요청을 처리하지 못했습니다.`로 실패했다.

### Cause

- Local/prod compose 설정이 섞이면서 `prod` profile 검증이 localhost 값을 거부해 백엔드가 재시작했다.
- `docker-compose.prod.yml`이 `APP_WEATHER_FALLBACK_ENABLED`를 `"false"`로 고정해 KMA 장애가 추천 생성 실패로 이어질 수 있었다.

### Fix

- `docker-compose.prod.yml`에서 `APP_WEATHER_FALLBACK_ENABLED`를 환경변수 기반으로 변경하고 기본값을 `true`로 했다.
- GCP public endpoint 기준 env 예시 `.env.gcp.example`을 추가했다.
  - `APP_FRONTEND_SUCCESS_URL=http://<GCP_VM_PUBLIC_IP>`
  - `APP_SECURITY_ALLOWED_ORIGINS=http://<GCP_VM_PUBLIC_IP>`
  - `GOOGLE_REDIRECT_URI=http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google`
  - `FRONTEND_PORT=80`
  - `SESSION_COOKIE_SECURE=false`
- `ProductionConfigurationValidator`가 `change-this...`와 `example.com` placeholder를 prod 값으로 허용하지 않도록 보강했다.

### Verification

- `./gradlew test --tests com.dailyoutfitweather.global.config.ProductionConfigurationValidatorTest --tests com.dailyoutfitweather.recommendation.WeatherSnapshotProviderTest --tests com.dailyoutfitweather.recommendation.RecommendationControllerIntegrationTest` passed.
- `./gradlew bootJar` passed and generated `backend/build/libs/daily-outfit-weather-backend-0.0.1-SNAPSHOT.jar`.
- `docker compose --env-file .env.gcp.example -f docker-compose.prod.yml config` passed.
- Local deployment-like Docker smoke passed with compose project `dow-local-smoke`, frontend port `18080`, `SPRING_PROFILES_ACTIVE=local`, `SESSION_COOKIE_SECURE=false`, and `APP_WEATHER_FALLBACK_ENABLED=true`.
- The local smoke rebuilt backend/frontend images, started PostgreSQL/backend/frontend containers, applied Flyway migrations, passed `scripts/deployment-smoke.sh`, returned 200 for `/`, returned expected 401 for unauthenticated `/api/me`, and returned 302 for `/oauth2/authorization/google`.
- Backend recent logs contained no `ERROR`, `Exception`, or `Restarting` pattern after the local smoke.

### Required Before GCP Retest

- `.env.gcp.example`을 GCP VM의 `.env`로 복사한 뒤 `change-this...` 값을 실제 secret으로 교체한다.
- Google Console Authorized redirect URI에 `http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google`을 등록한다.
- GCP VM에서 compose stack을 rebuild/recreate 한다.
- 로컬에서 로그인 후 추천 클릭까지 완전 검증하려면 Google Console에 `http://localhost:18080/login/oauth2/code/google`도 등록하거나, 이미 등록된 callback URI/port로 smoke stack을 띄워야 한다.

### Local OAuth Mismatch Follow-up

로컬 브라우저 로그인에서 Google `400 redirect_uri_mismatch`가 발생했다.

처음에는 기존 8080 stack 충돌을 피하려고 임시 `18080` smoke를 사용했기 때문에 callback URI도 `http://localhost:18080/login/oauth2/code/google`로 바뀌었다. 이 값은 사용자의 기존 로컬 검증 기준이 아니므로, primary local deployment stack을 다시 8080 기준으로 재기동했다.

현재 8080 local deployment stack은 Google authorization request에 다음 callback을 보낸다.

```text
http://localhost:8080/login/oauth2/code/google
```

이 URI가 Google Console의 OAuth client `Authorized redirect URIs`에 정확히 등록되어 있지 않으면 Google이 로그인을 차단한다. 포트, scheme, host, path가 모두 완전히 일치해야 한다.

8080 기준 검증 결과:

- `BASE_URL=http://127.0.0.1:8080 scripts/deployment-smoke.sh` passed.
- `daily-outfit-weather-backend-1`, `daily-outfit-weather-frontend-1`, `daily-outfit-weather-postgres-1` are `Up`.
- `/oauth2/authorization/google` returns 302 with `redirect_uri=http://localhost:8080/login/oauth2/code/google`.

## 2026-07-20 GCP Deployment Readiness & Docker Logging Limits

### Scope

GCP VM 배포 및 개인/가족 전용 PWA 이용 환경 준비 과정에서 Docker 컨테이너 로그 용량 제한 보완 및 GCP 전용 배포 가이드 문서(`docs/gcp-deployment-guide.md`)를 작성했다.

### Changes

- `docker-compose.prod.yml`:
  - `postgres`, `backend`, `frontend` 서비스에 Docker log max-size(`10m`, `max-file: 3`) 옵션 추가
- `docs/gcp-deployment-guide.md`:
  - GCP VM 구축, 외부 고정 IP 설정, VPC 방화벽 오픈 (포트 80/443), `.env` 설정, Google OAuth 테스트 사용자 등록 및 모바일 PWA "홈 화면에 추가" 체크리스트 문서화
- `docs/entrypoint.md`:
  - 중앙 문서 인덱스에 GCP 배포 가이드 링크 추가

