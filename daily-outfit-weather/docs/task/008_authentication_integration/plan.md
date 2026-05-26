# Plan: 008 Authentication Integration

## 작업 순서

1. **백엔드 의존성 추가**: `spring-boot-starter-security`, `spring-boot-starter-oauth2-client`를 `build.gradle`에 추가한다.
2. **보안 설정**: `SecurityConfig`를 작성하여 세션 정책, 권한 설정, OAuth2 로그인 설정을 구성한다.
3. **사용자 서비스**: `CustomOAuth2UserService`를 구현하여 Google 프로필 정보를 기반으로 `User`를 저장/업데이트한다.
4. **인증 정보 API**: `AuthController`를 추가하고 `GET /api/me`를 구현한다.
5. **권한 필터 적용**: 기존 API(`api/profile/**`, `api/recommendations/**`)에 인증 필터를 적용한다.
6. **프론트엔드 연동**: 
    - `App.tsx`에 로그인 화면 골격을 추가한다.
    - 로그인 버튼 클릭 시 `/oauth2/authorization/google`로 이동하게 한다.
    - 로그인 성공 후 리다이렉트 처리 및 세션 유지를 확인한다.
7. **로그아웃**: `/api/logout` 엔드포인트를 구성하고 프론트엔드에 버튼을 추가한다.

## 결정

- MVP 단계에서는 세션 방식을 사용하여 복잡도를 낮춘다.
- 로그인 완료 후 리다이렉트 경로는 프론트엔드 메인(`http://localhost:5173`)으로 설정한다.
- `.env.example`에 필요한 Google OAuth 환경 변수 항목을 추가한다.
