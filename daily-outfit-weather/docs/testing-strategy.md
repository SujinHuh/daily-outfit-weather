# Testing Strategy

테스트 전략 문서입니다.

대표 테스트 전략 문서는 이 파일입니다. `docs/project/standards/testing_profile.md`는 Harness Kit overlay용 요약 표준 문서입니다.

## 테스트 목표

- 추천 로직의 기준 온도 보정과 결과 선택을 안정적으로 검증한다.
- 사용자별 데이터 접근 제어를 검증한다.
- 외부 API 장애 또는 느린 응답에 대한 대체 흐름을 검증한다.
- 프론트엔드 주요 화면이 API 응답을 올바르게 표시하는지 검증한다.

## 백엔드 테스트

단위 테스트:

- RecommendationRuleEngine
- WeatherConditionAnalyzer
- OutfitSelector
- ItemSelector
- RecommendationMessageGenerator

통합 테스트:

- Profile API
- Recommendation API
- Feedback API
- NotificationLog 저장 흐름

테스트 DB 전략:

- DB 통합 테스트는 Testcontainers PostgreSQL을 사용합니다.
- Flyway migration은 Testcontainers PostgreSQL에서 검증합니다.
- 단위 테스트는 DB 없이 수행합니다.
- 외부 API 클라이언트는 fixture 또는 mock client를 기본으로 검증합니다.

## 프론트엔드 테스트

초기 기준:

- 빌드 검증
- 핵심 컴포넌트 렌더링 테스트
- 오늘 추천 화면 상태 테스트

Phase 1에서는 `npm run build`를 기본 검증으로 사용합니다. 컴포넌트 테스트 도구는 화면 기능 구현 단계에서 확장합니다.

## 외부 API 테스트 원칙

- 기상청 API 실제 호출은 통합 테스트 기본값으로 두지 않습니다.
- API 응답 fixture 또는 mock client를 우선 사용합니다.
- 실제 API 호출 검증은 별도 수동 검증 또는 제한된 smoke test로 분리합니다.

## Phase별 테스트 기준

- Phase 1: 백엔드 `./gradlew test`, 프론트엔드 `npm run build`, Docker Compose PostgreSQL 기동 확인
- Phase 2: 도메인과 프로필 API 테스트
- Phase 3: 추천 엔진 단위 테스트
- Phase 4: 오늘 추천 API 통합 테스트
- Phase 5: 날씨 API client mock/fixture 테스트
- Phase 6: 프론트엔드 빌드와 주요 화면 테스트
