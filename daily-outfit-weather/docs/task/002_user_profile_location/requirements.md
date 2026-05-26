# Requirements: 002 User Profile Location

## 기능 요구사항

- 온보딩 API로 사용자 닉네임, 민감도, 시간대, 교통수단, 말투, 알림 옵션, 집/직장 위치를 저장한다.
- 프로필 조회 API로 저장된 설정을 반환한다.
- 프로필 수정 API로 기존 설정과 집/직장 위치를 갱신한다.
- Phase 7 인증 전까지는 dev-only dummy user를 사용한다.

## 품질 요구사항

- 사용자당 UserProfile은 하나만 존재해야 한다.
- 사용자당 Location은 `HOME`, `WORK` 타입별 하나만 존재해야 한다.
- 민감도는 1~5 범위로 검증해야 한다.
- DB schema는 Flyway migration으로 관리한다.

## 검증 요구사항

- MockMvc 기반 API 통합 테스트를 작성한다.
- Testcontainers PostgreSQL로 JPA/Flyway 연동을 검증한다.
- `./gradlew test`가 성공해야 한다.
