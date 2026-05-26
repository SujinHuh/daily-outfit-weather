# Plan: 002 User Profile Location

## 작업 순서

1. User, UserProfile, Location JPA entity와 enum을 추가한다.
2. Flyway migration으로 `users`, `user_profiles`, `locations` 테이블을 생성한다.
3. Repository와 ProfileService를 구현한다.
4. `/api/profile/onboarding`, `/api/profile`, `/api/profile` PUT controller를 구현한다.
5. MockMvc + Testcontainers 통합 테스트를 작성한다.
6. `./gradlew test`로 검증한다.

## 비범위

- OAuth 인증 컨텍스트
- 위치 검색 API
- 추천 엔진 연동

## Phase 1 내부 감사 결과

- requirements는 development plan의 Phase 2 범위와 일치한다.
- plan은 issue의 저장/조회/수정 요구를 모두 포함한다.
- OAuth와 위치 검색은 후속 phase로 명시적으로 제외했다.
