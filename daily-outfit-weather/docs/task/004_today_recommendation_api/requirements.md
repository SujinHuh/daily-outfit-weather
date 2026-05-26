# Requirements: 004 Today Recommendation API

## 목표

- 사용자가 오늘 추천 결과를 조회할 수 있다.
- 오늘 추천이 없으면 생성 후 저장한다.
- 같은 사용자와 같은 날짜에는 추천을 중복 생성하지 않는다.

## 범위

- `GET /api/recommendations/today`
- `POST /api/recommendations/today`
- 추천 결과 저장 도메인과 repository
- Phase 3 추천 엔진과 프로필 정보 연결

## 비범위

- 실제 기상청 API 연동
- 추천 피드백
- 알림
- 프론트엔드 오늘 추천 화면
