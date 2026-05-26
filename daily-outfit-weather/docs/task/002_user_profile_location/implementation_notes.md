# Implementation Notes: 002 User Profile Location

## 진행 로그

- **2026-05-13:** Phase 2 문서 작성 및 내부 감사 완료.
- **2026-05-13:** User/UserProfile/Location 도메인, Flyway migration, Profile API 구현.
- **2026-05-13:** MockMvc + Testcontainers 통합 테스트 작성.
- **2026-05-13:** `./gradlew test` 성공. Docker Desktop 실행 후 Testcontainers PostgreSQL 기반으로 재검증 완료.
- **2026-05-13:** Phase 2 서브에이전트 검수 follow-up 반영. `PUT /api/profile`을 update-only로 분리하고, `PROFILE_NOT_FOUND`/`VALIDATION_FAILED` 에러 응답, 문자열 길이 검증, provider/providerId unique 제약, 실패 케이스 테스트를 추가했다.
- **2026-05-25:** Phase 5 follow-up으로 위치 입력값 기반 KMA 격자 좌표 자동 보강과 위치 검색 API를 추가했다.

## 구현 중 결정 사항

- Phase 7 인증 전까지 `dev-user@daily-outfit-weather.local` 고정 dev user를 사용한다.
- 위치 좌표 `nx`, `ny`는 요청값을 우선 사용하고, 요청에 없으면 정적 KMA grid catalog로 자동 보강한다. catalog에 없는 위치는 nullable로 유지한다.
- 온보딩 저장과 프로필 수정은 같은 upsert 로직을 공유한다.

## 사용자 승인 필요 항목

- 없음

## 후속 태스크 후보

- Phase 3: 추천 엔진 1차 구현
- Phase 5: 위치 입력값과 기상청 격자 좌표 매핑 보강 완료
