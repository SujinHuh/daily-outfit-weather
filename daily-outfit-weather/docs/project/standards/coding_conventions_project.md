# Coding Conventions & Structure

## 1. Backend (Java 21 / Spring Boot 3.3+)
- **JDK Version**: 최신 LTS인 **Java 21** (Record, Pattern Matching 활용)을 사용합니다.
- **Structure**: **도메인별 모듈 + 모듈 내부 Layered Architecture**를 따릅니다.
  - `com.dailyoutfitweather.{module}.{controller, service, domain, repository, dto}`
- **Standard**: 
  - 비즈니스 로직은 도메인 엔티티 또는 서비스에 응집합니다.
  - 공통 응답 객체(`ApiResponse<T>`) 도입 여부는 아직 결정 전이며, 확정 전까지는 `docs/api-spec.md`를 우선합니다.
  - `@Transactional`을 명확히 사용하여 트랜잭션 범위를 제어합니다.

## 2. Frontend (React / Vite / TypeScript)
- **Structure**: 기능별(Feature-based) 구조를 사용합니다.
  - `src/features/{feature_name}/{components, hooks, services, types}`
- **State Management**: 전역 상태는 최소화하며, 필요 시 `Context API` 또는 `Zustand`를 고려합니다.
- **Styling**: CSS Modules 또는 Vanilla CSS를 사용하여 컴포넌트 간 스타일 간섭을 방지합니다.

## 3. 공통 규칙
- **Test First**: 핵심 비즈니스 로직(추천 엔진 등)은 반드시 단위 테스트를 포함해야 합니다.
- **Clean Code**: 함수/메서드는 하나의 일만 수행하며, 길이는 20줄을 넘지 않도록 노력합니다.
