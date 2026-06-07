# Security Deployment Policy

이 문서는 운영 배포 전 확정해야 하는 CSRF, CORS, cookie, OAuth smoke test 기준을 정리한다.

## 현재 상태

- 인증은 Spring Security OAuth2 Login과 서버 세션 쿠키를 사용한다.
- CSRF 보호는 `XSRF-TOKEN` 쿠키와 `X-XSRF-TOKEN` 헤더 방식으로 활성화되어 있다.
- 프론트엔드 개발 서버는 Vite proxy로 `/api`, `/oauth2`, `/login` 요청을 백엔드로 전달한다.
- 알림 로그 생성용 `POST /api/notifications/generate-due`는 내부 작업 토큰 `X-Internal-Job-Token`으로 보호한다.
- 운영 same-origin 배포는 `frontend/nginx.conf`와 `docker-compose.prod.yml` 기준으로 구성한다.

## 운영 전 필수 정책

### Domain and TLS

- 운영 public origin을 먼저 하나로 확정한다. 예: `https://daily-outfit-weather.example.com`.
- DNS는 해당 host가 실제 배포 entrypoint를 가리키도록 설정한다.
  - 단일 VM/로드밸런서라면 `A`/`AAAA` 또는 provider 지침의 `CNAME`을 사용한다.
  - Cloudflare, ALB, Render, Fly.io 같은 외부 proxy가 TLS를 종료한다면 origin으로 HTTP를 전달하더라도 browser-facing URL은 반드시 HTTPS여야 한다.
- TLS 인증서는 운영 public origin의 host와 정확히 일치해야 한다.
  - wildcard 인증서를 쓰는 경우에도 OAuth redirect URI에 등록할 host가 인증서 SAN에 포함되어야 한다.
  - 브라우저에서 인증서 chain 오류, mixed content 오류, HTTP downgrade가 없어야 한다.
- TLS 종료 proxy는 backend로 다음 header를 전달해야 한다.
  - `Host: <운영 host>`
  - `X-Forwarded-Proto: https`
  - `X-Forwarded-Host: <운영 host>` 또는 원래 `Host` 유지
  - `X-Forwarded-For: <client ip>`
- 현재 container nginx는 `/api`, `/oauth2`, `/login`을 backend로 proxy하며 `X-Forwarded-Proto`를 `$scheme`으로 전달한다. 이 nginx 앞단에서 TLS를 종료하는 경우 앞단 proxy가 nginx로 `https` scheme을 보존하거나 같은 header를 backend까지 전달하도록 구성한다.
- 운영 환경 변수는 public origin과 일치해야 한다.
  - `APP_FRONTEND_SUCCESS_URL=https://<운영 host>`
  - `APP_SECURITY_ALLOWED_ORIGINS=https://<운영 host>`
  - `SESSION_COOKIE_SECURE=true`
  - `SESSION_COOKIE_SAME_SITE=lax` for same-site deployment
- 프론트엔드와 백엔드를 서로 다른 site로 분리 배포하는 경우에만 `SESSION_COOKIE_SAME_SITE=none`과 `SESSION_COOKIE_SECURE=true` 조합을 검토한다. 이 경우 CORS allowed origin은 프론트엔드 origin만 허용한다.

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

- `.env` 또는 운영 secret store에 `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `APP_FRONTEND_SUCCESS_URL`, `APP_SECURITY_ALLOWED_ORIGINS`가 설정되어 있다.
- Google Console OAuth consent screen이 publish 또는 test user 허용 상태이며, smoke test에 사용할 Google 계정이 접근 가능하다.
- Google Console OAuth client의 authorized redirect URI가 실행 환경과 정확히 일치한다.
  - 로컬 백엔드 직접 실행: `http://localhost:8080/login/oauth2/code/google`
  - 로컬 Vite proxy 실행: `http://localhost:5173/login/oauth2/code/google`
  - 로컬 prod compose 실행: `http://localhost:${FRONTEND_PORT:-8080}/login/oauth2/code/google`
  - 운영 same-origin 배포: `https://<운영 host>/login/oauth2/code/google`
- 운영 smoke test는 DNS와 TLS가 먼저 정상이어야 한다.
  - `https://<운영 host>/api/health`가 인증서 오류 없이 200을 반환한다.
  - `https://<운영 host>/oauth2/authorization/google` 요청이 Google 인증 URL로 302 이동한다.
  - redirect URL의 `redirect_uri` query 값이 Google Console에 등록한 운영 URI와 완전히 동일하다.

Google Console 등록 절차:

1. Google Cloud Console에서 해당 project를 연다.
2. `APIs & Services` > `Credentials` > 운영 OAuth 2.0 Client ID를 연다.
3. `Authorized JavaScript origins`에 운영 origin을 등록한다. 예: `https://<운영 host>`.
4. `Authorized redirect URIs`에 Spring Security callback을 등록한다. 예: `https://<운영 host>/login/oauth2/code/google`.
5. 로컬 smoke test도 유지하려면 위 로컬 URI를 같은 client 또는 별도 dev client에 등록한다.
6. 저장 후 변경 사항이 전파될 때까지 기다린 뒤 브라우저 smoke test를 수행한다.

검증 기준:

- 새 incognito/private browser session에서 운영 public origin을 연다.
- 로그인 버튼 클릭 시 `/oauth2/authorization/google`로 이동하고 Google 로그인 화면이 표시된다.
- Google 인증 화면에서 표시되는 app name과 계정 접근 범위가 의도한 OAuth consent screen과 일치한다.
- 인증 완료 후 `APP_FRONTEND_SUCCESS_URL`로 돌아오며 URL에 OAuth `code` 또는 `state`가 남아 있지 않다.
- browser devtools Network에서 `/login/oauth2/code/google`이 302 후 앱으로 돌아오고, 최종 page load가 200이다.
- Application/Storage에서 `JSESSIONID`가 발급되어 있고 운영 HTTPS에서는 Secure 속성이 적용되어 있다.
- `GET /api/me`가 로그인 사용자를 반환한다.
- 온보딩 저장, 오늘 추천 생성, 피드백 저장 요청이 401/403/CSRF 오류 없이 동작한다.
- page refresh 후에도 로그인 세션이 유지된다.
- 로그아웃 후 `JSESSIONID`가 제거되거나 무효화되고 보호 API가 401을 반환한다.
- 다시 로그인하면 기존 Google 계정 사용자로 연결되며 중복 사용자 생성이 없는지 확인한다.

실패 시 우선 확인할 항목:

- Google `redirect_uri_mismatch`: Google Console redirect URI와 실제 request의 `redirect_uri`가 scheme, host, port, path까지 동일한지 확인한다.
- 로그인 후 HTTP로 돌아감 또는 Secure cookie 미발급: TLS proxy의 `X-Forwarded-Proto: https` 전달과 `server.forward-headers-strategy=framework` 적용을 확인한다.
- 로그인 직후 401: session cookie domain/path/SameSite/Secure와 frontend/backend origin 구성을 확인한다.
- mutating 요청 403: `XSRF-TOKEN` cookie 발급과 `X-XSRF-TOKEN` request header 전송을 확인한다.

## 2026-06-07 로컬 확인 결과

- Docker/Testcontainers 기반 백엔드 전체 테스트가 통과했다.
- `.env` 기반 백엔드 부팅, PostgreSQL/Flyway 연결, `/api/health` 응답을 확인했다.
- Google OAuth redirect가 Google 인증 URL로 302 이동하는 것을 확인했다.
- KMA live API가 `NORMAL_SERVICE`를 반환하는 것을 확인했다.
- 브라우저 기반 최종 OAuth 로그인 완료 flow는 운영 도메인 redirect URI 등록 후 재검증해야 한다.
