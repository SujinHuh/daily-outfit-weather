# Implementation Notes: 008 Authentication Integration

## 2026-06-14 CSRF 403 Forbidden Debugging

Google OAuth 로그인 후 온보딩 저장(`POST /api/profile/onboarding`) 시 403 Forbidden 에러가 발생하는 현상을 조사하고 수정했다.

### 조사 내용

1.  **현상**: 사용자는 성공적으로 로그인하여 `/api/me`에서 정보를 가져오지만, 프로필 저장 시에만 403 에러가 발생함.
2.  **원인 1 (CSRF Bootstrap)**: SPA 환경에서 로그인 직후 첫 mutating 요청 전까지 `XSRF-TOKEN` 쿠키가 발급되지 않아 헤더에 토큰을 실어 보내지 못함.
3.  **원인 2 (Spring Security 6 Deferred Token)**: Spring Security 6의 기본 설정인 `XorCsrfTokenRequestAttributeHandler`는 XOR 연산된 토큰을 기대하지만, 프론트엔드는 쿠키의 원본(raw) 토큰을 보냄. 또한, 토큰이 지연 로딩되면서 필터에서 강제로 토큰을 꺼내지 않으면 쿠키가 생성되지 않는 문제가 있음.

### 수정 내역

1.  **SecurityConfig.java (Backend)**:
    *   `CsrfTokenRequestAttributeHandler`를 설정하고 `setCsrfRequestAttributeName(null)`을 호출하여 Spring Security 6의 기본 XOR 핸들링을 비활성화했습니다. 이는 SPA에서 쿠키의 원본 토큰을 헤더로 보낼 때 필수적인 설정입니다.
    *   `CsrfCookieFilter`에서 `request.getAttribute(CsrfToken.class.getName())`을 사용하여 지연 로딩된 토큰의 생성을 강제했습니다.
2.  **App.tsx (Frontend)**:
    *   `request` 헬퍼 함수에서 mutating 요청 전 `ensureCsrfCookie`를 통해 `XSRF-TOKEN` 쿠키 존재 여부를 확인하고, 없으면 `/api/health`를 호출하여 쿠키를 발급받는 로직을 강화함.
    *   디버그 로그(`[API] Sending ... with X-XSRF-TOKEN`)를 추가하여 브라우저에서 토큰 전송 여부를 확인할 수 있게 함.

### 검증 결과

*   `docs/deployment-readiness-log.md`에 해당 이슈와 해결 방법을 기록함.
*   로컬 환경에서 CSRF 토큰 발급 및 전송 흐름을 논리적으로 재검증함.
