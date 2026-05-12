# Testing Profile

이 문서는 Harness Kit overlay용 테스트 표준 요약입니다.

대표 테스트 전략 문서는 [../../testing-strategy.md](../../testing-strategy.md)입니다.

## 1. 전략
- **Unit Testing**: 비즈니스 로직(추천 엔진, 기상 분석 등)에 집중합니다.
- **Integration Testing**: API 엔드포인트와 DB 연동을 검증합니다.
- **Verification First**: 구현 전 테스트 케이스를 먼저 정의하거나 작성하는 것을 지향합니다.

## 2. Backend (Java)
- **Framework**: JUnit 5, AssertJ, Mockito
- **API Test**: `MockMvc`를 활용하여 컨트롤러 계층을 테스트합니다.
- **Database Test**: DB 통합 테스트는 Testcontainers PostgreSQL을 사용합니다. 필요한 경우 `@DataJpaTest`도 Testcontainers 기반 PostgreSQL과 함께 사용합니다.

## 3. Frontend (TS)
- **Framework**: Vitest
- **Component Test**: React Testing Library
- **Validation**: PWA 기능은 브라우저 개발자 도구 및 Lighthouse를 통해 수동/자동 검증합니다.
