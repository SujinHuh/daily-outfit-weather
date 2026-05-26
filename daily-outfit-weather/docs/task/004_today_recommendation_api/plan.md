# Plan: 004 Today Recommendation API

## 작업 순서

1. `outfit_recommendations` migration과 JPA entity를 추가한다.
2. 오늘 추천 조회/생성 repository query를 추가한다.
3. Phase 3 추천 엔진을 호출하는 application service를 추가한다.
4. `GET /api/recommendations/today`, `POST /api/recommendations/today` controller를 추가한다.
5. 통합 테스트로 생성, 재사용, 프로필 없음 오류를 검증한다.

## 결정

- Phase 5 전까지 날씨 API 입력은 `WeatherSnapshotProvider`의 기본 스냅샷으로 분리한다.
- GET과 POST 모두 같은 get-or-create 동작을 사용한다.
