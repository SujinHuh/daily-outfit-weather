# Deployment Readiness Log

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
