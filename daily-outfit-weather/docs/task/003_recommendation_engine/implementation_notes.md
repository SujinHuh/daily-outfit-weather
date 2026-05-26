# Implementation Notes: 003 Recommendation Engine

## 진행 로그

- **2026-05-13:** Phase 3 문서 작성 및 내부 감사 완료.
- **2026-05-13:** 추천 입력/결과 DTO와 룰 기반 추천 엔진 컴포넌트 구현.
- **2026-05-13:** 체감온도, 강풍, 강수, 눈, 말투 변화 단위 테스트 작성.
- **2026-05-13:** `./gradlew test` 성공. 기존 Testcontainers 통합 테스트와 Phase 3 단위 테스트를 함께 검증했다.
- **2026-05-13:** Phase 3 서브에이전트 검수 follow-up 반영. 퇴근길 하락 중복 보정, 퇴근 시간 비 예보 작은 우산 판정, 눈 예보 우산 오추천, 입력 DTO invariant, API singular item mapping 리스크를 보완했다.

## 구현 중 결정 사항

- Phase 3 엔진은 DB, 외부 API, 사용자 repository에 의존하지 않는 순수 컴포넌트로 유지한다.
- 강수확률 60% 이상 또는 강수유형 `RAIN`/`SNOW`면 준비물을 추천한다.
- 풍속 4.0m/s 이상이면 강풍으로 보고 기준 온도를 낮추며 바람막이를 추천한다.
- 엔진 내부는 여러 준비물을 `itemRecommendations`로 유지하고, Phase 4 API draft의 단일 `itemRecommendation` 필드를 위해 comma-joined singular 필드도 함께 반환한다.

## 사용자 승인 필요 항목

- 없음

## 후속 태스크 후보

- Phase 4: 오늘 추천 API와 추천 결과 저장
- Phase 5: 실제 기상청 API 입력값을 `RecommendationInput`으로 변환
