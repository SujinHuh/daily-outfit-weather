# Plan: 003 Recommendation Engine

## 작업 순서

1. 추천 입력/결과 DTO를 추가한다.
2. 출근/퇴근 날씨를 분석하는 `WeatherConditionAnalyzer`를 구현한다.
3. 추천 기준 온도를 계산하는 `RecommendationRuleEngine`을 구현한다.
4. 기준 온도별 옷차림을 고르는 `OutfitSelector`를 구현한다.
5. 날씨 조건별 준비물을 고르는 `ItemSelector`를 구현한다.
6. 말투별 추천 문구를 생성하는 `RecommendationMessageGenerator`를 구현한다.
7. `RuleBasedRecommendationEngine`으로 전체 흐름을 조합한다.
8. 단위 테스트와 전체 백엔드 테스트를 실행한다.

## 비범위

- 추천 저장
- 추천 API
- 날씨 API client
- 사용자 프로필 repository 연동

## Phase 1 내부 감사 결과

- development plan Phase 3의 작업 항목을 모두 plan에 반영했다.
- Phase 4의 API/저장 책임은 명시적으로 제외했다.
- Phase 5의 실제 날씨 API 연동도 제외했다.
