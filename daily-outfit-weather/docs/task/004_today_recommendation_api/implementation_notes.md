# Implementation Notes: 004 Today Recommendation API

## 진행 로그

- **2026-05-25:** Phase 4 문서 초안 작성.
- **2026-05-25:** `outfit_recommendations` migration, entity, repository 추가.
- **2026-05-25:** 오늘 추천 GET/POST API와 get-or-create service 추가.
- **2026-05-25:** Phase 5 전까지 교체 가능한 기본 `WeatherSnapshotProvider`를 추가.
- **2026-05-25:** 오늘 추천 API 통합 테스트를 추가하고 컴파일 검증을 완료. 전체 통합 테스트는 Docker/Testcontainers 미탐지로 보류.
- **2026-05-25:** 서브 에이전트 검수 반영. 사용자 row lock 기반 get-or-create, `Asia/Seoul` Clock 주입, 원본 날씨 snapshot 저장, service 단위 테스트를 추가했다.

## 구현 중 결정 사항

- 프로필이 없으면 기존 `PROFILE_NOT_FOUND` 오류를 반환한다.
- 사용자/날짜 unique 제약으로 중복 생성을 방지한다.
- 추천 API 응답의 `weatherSummary`는 저장된 `weather_snapshot` JSONB에서 반환한다.

## 후속 태스크 후보

- Phase 5: 실제 기상청 API 결과를 `WeatherSnapshotProvider`에 연결한다.
