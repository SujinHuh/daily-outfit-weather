# Security Deployment Policy

이 문서는 운영 배포 전 확정해야 하는 CSRF, CORS, cookie, OAuth smoke test 기준을 정리한다.

## 현재 상태

- 인증은 Spring Security OAuth2 Login과 서버 세션 쿠키를 사용한다.
- MVP 개발 편의상 CSRF 보호는 `SecurityConfig`에서 비활성화되어 있다.
- 프론트엔드 개발 서버는 Vite proxy로 `/api`, `/oauth2`, `/login` 요청을 백엔드로 전달한다.
- 알림 로그 생성용 `POST /api/notifications/generate-due`는 내부 작업 토큰 `X-Internal-Job-Token`으로 보호한다.

## 운영 전 필수 정책

### CSRF

- 세션 쿠키 기반 인증을 유지한다면 운영에서는 CSRF 보호를 활성화한다.
- SPA는 CSRF 토큰을 쿠키 또는 bootstrap API로 받은 뒤 mutating 요청에 `X-XSRF-TOKEN` 헤더를 포함한다.
- CSRF 보호 대상은 `POST`, `PUT`, `PATCH`, `DELETE` 요청이다.
- OAuth callback, health check, 정적 자산은 CSRF 검증 대상에서 제외할 수 있다.

### CORS

- 운영 CORS 허용 origin은 배포된 프론트엔드 origin만 허용한다.
- 개발 환경에서는 `http://localhost:5173`만 허용한다.
- `Access-Control-Allow-Credentials`는 세션 쿠키 사용 때문에 `true`가 필요하다.
- wildcard origin `*`는 credentials와 함께 사용하지 않는다.

### Cookie

- 운영 세션 쿠키는 `HttpOnly`, `Secure`, `SameSite=Lax`를 기본값으로 한다.
- 프론트엔드와 백엔드가 서로 다른 site로 배포되어 OAuth/세션 흐름에 문제가 생기면 `SameSite=None; Secure`를 검토한다.
- 로컬 개발에서는 HTTPS가 없을 수 있으므로 `Secure` 강제를 환경별로 분리한다.
- 로그아웃 시 `JSESSIONID` 삭제와 서버 세션 무효화를 유지한다.

### Internal Job Token

- `APP_NOTIFICATION_GENERATE_DUE_TOKEN`은 운영 secret store에서 주입한다.
- 토큰이 비어 있으면 `POST /api/notifications/generate-due`는 403을 반환한다.
- 이 엔드포인트는 외부 사용자 UI에서 호출하지 않고 스케줄러, 배치, 운영 작업에서만 호출한다.

## Google OAuth Smoke Test

실제 브라우저 smoke test는 다음 조건이 모두 충족될 때 수행한다.

- `.env`에 `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `APP_FRONTEND_SUCCESS_URL`이 설정되어 있다.
- Google Console OAuth redirect URI에 `http://localhost:8080/login/oauth2/code/google`이 등록되어 있다.
- Docker Desktop 또는 PostgreSQL이 실행 중이다.
- 백엔드 `http://localhost:8080`, 프론트엔드 `http://localhost:5173`이 동시에 실행 중이다.

검증 기준:

- 로그인 버튼 클릭 시 Google 로그인 화면으로 이동한다.
- Google 인증 완료 후 `APP_FRONTEND_SUCCESS_URL`로 돌아온다.
- `GET /api/me`가 로그인 사용자를 반환한다.
- 온보딩/오늘 추천/피드백 요청이 401 없이 동작한다.
- 로그아웃 후 보호 API가 401을 반환한다.

## 2026-05-26 로컬 확인 결과

- `.env` 파일이 없어 실제 Google OAuth Client ID/Secret 확인을 진행할 수 없었다.
- Docker daemon이 실행 중이 아니어서 PostgreSQL/Testcontainers 기반 smoke test를 진행할 수 없었다.
- 따라서 실제 Google OAuth 브라우저 smoke test는 환경 준비 후 재실행해야 한다.
