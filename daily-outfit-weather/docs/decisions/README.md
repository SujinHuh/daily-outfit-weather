# Decision Records

프로젝트 구현 중 결정해야 할 항목과 확정된 결정을 기록합니다.

## 작성 원칙

- 구현에 영향을 주는 결정은 이 문서 또는 별도 `DEC-xxx-*.md`로 남깁니다.
- 결정 전 항목은 `Pending`으로 둡니다.
- 결정 후에는 결정 이유와 영향을 간단히 기록합니다.

## Pending Decisions

| ID | 상태 | 결정 항목 | 영향 |
| --- | --- | --- | --- |
| - | - | 현재 pending decision 없음 | - |

## 확정된 결정

| ID | 결정 | 이유 | 영향 |
| --- | --- | --- | --- |
| DEC-001 | Java 21을 사용한다. | 최신 LTS이며 Spring Boot 3.x와 잘 맞는다. | 백엔드 프로젝트 생성과 로컬 개발 문서는 Java 21 기준으로 작성한다. |
| DEC-002 | Spring Boot 3.x를 사용한다. | 현재 Spring Boot 표준 계열이며 Java 21과 호환된다. | 정확한 patch 버전은 프로젝트 생성 시점의 안정 버전으로 선택한다. |
| DEC-003 | 프론트엔드 패키지 매니저는 npm을 사용한다. | Vite 기본 흐름과 문서 호환성이 좋고 MVP에 충분하다. | 프론트엔드 명령은 `npm run ...` 기준으로 문서화한다. |
| DEC-004 | DB 마이그레이션 도구는 Flyway를 사용한다. | SQL 파일 기반이라 이해하기 쉽고 Spring Boot와 연동이 단순하다. MVP와 포트폴리오 목적에 충분하다. | DB 스키마 변경은 `db/migration` 아래 Flyway SQL 파일로 관리한다. |
| DEC-005 | 테스트 DB는 Testcontainers PostgreSQL을 사용한다. | 실제 운영 DB가 PostgreSQL이고 Flyway 마이그레이션을 검증해야 하므로 H2나 embedded DB와의 동작 차이를 줄인다. | DB 통합 테스트는 Testcontainers 기반 PostgreSQL에서 실행하고, 단위 테스트는 DB 없이 수행한다. 외부 API는 fixture/mock client를 기본으로 한다. |
| DEC-006 | OAuth 인증 방식은 Session Cookie 기반으로 시작한다. | Spring Security OAuth2 Login과 자연스럽고 MVP 배포 구조에서 단순하다. | Phase 7 이후 사용자 데이터 API는 인증된 세션 사용자 기준으로 접근 제어한다. |
| DEC-007 | API 에러 응답은 `code`와 `message`를 기본 필드로 사용한다. | 프론트엔드가 에러를 안정적으로 분기하고 사용자 메시지를 표시하기 쉽다. | 상세 validation error 형식은 API 구현 전 보강한다. |
| DEC-008 | 기본 timezone은 `Asia/Seoul`로 한다. | 한국 날씨와 출근/퇴근 시간 기준 서비스다. | 날짜, 추천 생성, 알림 기준 시간은 `Asia/Seoul`을 기본으로 한다. |
| DEC-009 | 기상청 좌표 매핑은 1차에서 정적 CSV/JSON 데이터로 시작한다. | 지도/주소 API 없이 동 단위 검색을 빠르게 구현할 수 있다. | 위치 검색과 예보 좌표 매핑은 정적 데이터 기반으로 먼저 구현한다. |
| DEC-010 | Harness Kit는 MVP bootstrap 동안 partial overlay를 유지한다. | 현재 대표 문서와 경량 task workspace가 이미 있고, `docs/project/*`는 overlay 보조 진입점 역할을 하고 있다. 정식 overlay는 validator 실행과 복잡한 다단계 작업이 필요해질 때 도입해도 충분하다. | `docs/decisions`, `docs/testing-strategy`, `docs/task/*`를 canonical source로 두고, `docs/project/*`는 포인터 문서로 유지한다. |
| DEC-011 | 백엔드 빌드 도구는 Gradle을 사용한다. | Spring Boot 프로젝트에서 널리 쓰이고, Wrapper 기반으로 로컬/CI 실행 명령을 고정하기 쉽다. | 백엔드 실행/테스트 명령은 `./gradlew bootRun`, `./gradlew test`를 기준으로 문서화한다. |
| DEC-012 | 백엔드 패키지명은 `com.dailyoutfitweather`를 사용한다. | 레포명과 서비스 목적을 반영하며 기존 `weatherfit` legacy 이름과 구분된다. | Spring Boot application class와 패키지 구조는 `com.dailyoutfitweather` 기준으로 생성한다. |
| DEC-013 | Node.js 24 LTS를 사용한다. | 2026-05-12 기준 Node.js 24는 Active LTS이며 React/Vite 개발 환경을 장기간 안정적으로 고정할 수 있다. | `.nvmrc`는 `24`, 프론트엔드 `package.json` engines는 `>=24 <25` 기준으로 작성한다. |
| DEC-014 | Docker Compose 서비스명은 `postgres`, DB명과 사용자는 `daily_outfit_weather`를 사용한다. | 서비스명은 Compose 내부 DNS로 직관적이고, DB명/사용자명은 레포명과 일치하면서 PostgreSQL 식별자 관례에 맞는다. | 로컬 DB 기본값은 `POSTGRES_DB=daily_outfit_weather`, `POSTGRES_USER=daily_outfit_weather`, `POSTGRES_PASSWORD=daily_outfit_weather`, `POSTGRES_PORT=5432`로 둔다. 로컬 JDBC URL은 `jdbc:postgresql://localhost:5432/daily_outfit_weather`를 사용한다. |
| DEC-015 | 아키텍처는 Modular Monolith + Layered Architecture를 따른다. | MVP 복잡도를 낮추면서도 모듈 경계를 유지할 수 있다. | 단일 Spring Boot 앱 안에서 도메인별 모듈과 모듈 내부 계층을 유지한다. |
| DEC-016 | `characterImageType`은 추천 결과에 저장한다. | 추천 당시 어떤 이미지 타입이 선택됐는지 기록으로 남길 수 있다. | `outfit_recommendations.character_image_type`을 유지한다. |
| DEC-017 | DB enum 값은 MVP 1차에서 애플리케이션 레벨로 검증하고 DB check constraint는 보류한다. | 초기 도메인 enum은 구현 중 변경 가능성이 높아 check constraint를 먼저 강제하면 마이그레이션 비용이 커진다. | Flyway 초기 DDL에는 enum check constraint를 넣지 않고, 요청 validation과 도메인 enum 변환에서 유효성을 보장한다. |
