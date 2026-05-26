# Requirements: 008 Authentication Integration

Phase 7 목표: Google OAuth2 기반 인증 시스템을 구축하고 프론트엔드와 연결한다.

## 기능 요구사항

- [x] Google OAuth2 로그인을 통해 사용자 인증을 수행한다.
- [x] 첫 로그인 시 사용자(`User`) 엔티티를 자동으로 생성한다.
- [x] 로그인 성공 시 세션 쿠키를 발급한다.
- [x] `GET /api/me`를 통해 현재 로그인된 사용자 정보를 조회할 수 있다.
- [x] 인증되지 않은 사용자가 보호된 API에 접근할 경우 401 Unauthorized를 반환한다.
- [x] 프론트엔드에 Google 로그인 버튼을 추가하고 로그인 상태를 관리한다.

## 기술 요구사항

- Spring Security 6.x와 `oauth2-client`를 사용한다.
- Session-based authentication 방식을 사용한다 (MVP).
- `CustomOAuth2UserService`를 구현하여 사용자 정보를 영속화한다.
- CSRF 보호 설정은 MVP 개발 편의를 위해 비활성화하되, 운영 배포 전 보호 전략을 별도 확정한다.
- `SecurityConfig`에서 API 경로별 권한을 설정한다.
- 프론트엔드에서는 `/api/me` 호출 결과에 따라 로그인 여부를 판단하고, `/api/profile`의 404로 온보딩 필요 여부를 판단한다.

## 비기능 요구사항

- OAuth 로그인 성공 후 이동할 프론트엔드 URL은 환경 변수로 관리한다.
- 클라이언트 ID와 시크릿은 환경 변수로 관리하며 코드로 노출하지 않는다.
- 로그아웃 기능을 제공한다.
