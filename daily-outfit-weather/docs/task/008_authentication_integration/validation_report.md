# Validation Report: 008 Authentication Integration

## 상태

**Phase 7 수정 검증 완료**

실제 Google OAuth2 로그인은 유효한 Google Client ID/Secret 및 Google Console redirect URI 설정이 필요하므로 로컬 자동 테스트 범위에서는 제외했다.

## 반영한 검수 피드백

- [x] 프론트 dev server에서 `/oauth2/**`, `/login/**` 요청을 백엔드로 프록시하도록 수정했다.
- [x] `SecurityConfig`의 로그인 성공 URL 하드코딩을 `app.frontend.success-url` / `APP_FRONTEND_SUCCESS_URL` 설정으로 분리했다.
- [x] `@LoginUser`가 인증 사용자나 DB 사용자를 찾지 못할 때 `401 Unauthorized`를 반환하도록 수정했다.
- [x] 기존 email 계정이 있는 상태에서 Google 로그인을 시도할 때 provider 정보를 연결해 unique constraint 충돌을 피하도록 수정했다.
- [x] `RecommendationServiceTest`와 컨트롤러 통합 테스트를 인증 사용자 주입 흐름에 맞게 갱신했다.
- [x] 프론트 unused catch binding lint 오류를 수정했다.

## 실행한 검증

- `npm run lint` 성공
- `npm run build` 성공
- `./gradlew test` 성공

## 남은 리스크

- CSRF는 MVP 개발 편의를 위해 아직 비활성화되어 있다. 세션 쿠키 기반 운영 배포 전에는 CSRF 토큰, SameSite, CORS, 배포 origin 정책을 확정해야 한다.
- 실제 Google OAuth2 로그인은 브라우저에서 Google Client 설정과 함께 별도 smoke test가 필요하다.
