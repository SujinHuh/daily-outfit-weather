# Review Notes

이 문서는 기존 README 초안을 검수한 결과와 정제 결정을 기록합니다.

주의: 이 문서는 시간순 검수 로그입니다. 아래에는 당시에는 문제였지만 이후 반영 완료된 항목도 남아 있습니다. 현재 구현 기준은 `README.md`와 각 대표 문서를 우선합니다.

## 검수 결과

기존 README 초안은 프로젝트 이해에는 도움이 되지만 GitHub 첫 화면 README로는 부적절했습니다.

이유:

- 전체 길이가 너무 길었습니다.
- 제품 기획, DB 설계, API 명세, 개발 단계, Codex 지시서가 한 파일에 섞여 있었습니다.
- 레포명 후보 `weather-fit`과 실제 폴더명 `daily-outfit-weather`가 섞여 있었습니다.
- Harness Kit 사용 조건이 반영되어 있지 않았습니다.
- 실행 방법, 환경변수, 로컬 개발 방법이 없었습니다.
- 기상청 단기예보 API에서 바로 제공되지 않는 값까지 MVP 데이터처럼 섞여 있었습니다.
- README의 MVP 최종 기능과 초기 구현 순서가 구분되어 있지 않았습니다.

## 정제 결정

### README

README는 프로젝트 첫 화면용으로 줄였습니다.

포함한 내용:

- 프로젝트 소개
- MVP 목표
- 핵심 기능
- 제외 범위
- 기술 스택
- 예상 구조
- 주요 문서 링크
- 개발 원칙
- 다음 단계

### 원본 기획 문서

기존 긴 README는 삭제하지 않고 `docs/project-brief.md`로 보존했습니다.

이 문서는 다음 용도로 사용합니다.

- 서비스 기획 원본
- 도메인 후보 검토
- API/DB 설계 초안 참고
- 향후 확장 기능 후보 관리

### 구현 계획

실제 구현 순서는 `docs/development-plan.md`로 분리했습니다.

원본 MVP 범위가 크기 때문에 구현은 다음 방식으로 축소했습니다.

- 먼저 인증/외부 API 없이 추천 흐름을 만든다.
- 이후 사용자/프로필/위치 도메인을 붙인다.
- Google OAuth와 기상청 API는 별도 Phase에서 연결한다.

### Harness Kit

Harness Kit 사용 기준은 `docs/harness-kit-notes.md`로 분리했습니다.

현재 프로젝트는 완전한 빈 폴더가 아니므로, Harness Kit 기준으로는 기존 프로젝트 첫 도입 방식이 적절합니다.

## 추가 검수 필요 항목

- Google OAuth 인증 방식을 세션 쿠키로 할지 JWT로 할지 결정해야 합니다.
- 기상청 API 외에 미세먼지/자외선 데이터를 실제로 다룰지 결정해야 합니다.
- DB 마이그레이션 도구로 Flyway를 사용할지 Liquibase를 사용할지 결정해야 합니다.
- 프론트와 백엔드를 하나의 레포에서 어떤 방식으로 빌드/배포할지 결정해야 합니다.
- Harness Kit overlay 문서를 실제로 생성할지, 경량 문서로만 운영할지 결정해야 합니다.

## 서브에이전트 검수 반영

서브에이전트 검수 후 다음 내용을 반영했습니다.

- README에서 `MVP 최종 포함 기능`과 `초기 구현 순서`를 분리했습니다.
- README의 현재 상태를 `Harness Kit 기준 개발 계획 작성 완료`에서 `경량 개발 계획 작성 완료, Harness Kit overlay 도입 여부 미확정`으로 낮춰 표현했습니다.
- `docs/project-brief.md` 제목에 `Original Brief`를 표시했습니다.
- `Weather Fit` / `weather-fit`은 원본 아이디어 이름이며 구현 기준 이름이 아님을 명시했습니다.
- 미세먼지/자외선 실제 API 연동은 1차 구현 제외 또는 후순위 대상으로 명시했습니다.

## 당시 권장 다음 작업

`001_project_bootstrap` 작업을 생성하고, Harness Kit 기준으로 Phase 1 문서부터 작성합니다.

현재 이 작업 문서 생성은 완료되었습니다.

첫 구현 범위:

- `backend` 생성
- `frontend` 생성
- `docker-compose.yml` 생성
- `.env.example` 생성
- README 실행 방법 추가

## 문서 추가 분리

`docs/project-brief.md`가 원본 보관소로는 유용하지만 실제 개발 참조 문서로는 너무 커서 다음 문서로 분리했습니다.

- `docs/product-requirements.md`
- `docs/screen-flow.md`
- `docs/domain-model.md`
- `docs/database-design.md`
- `docs/api-spec.md`
- `docs/recommendation-logic.md`
- `docs/notification-policy.md`
- `docs/architecture.md`
- `docs/privacy-policy-notes.md`

`docs/project-brief.md`는 원본 보존용으로 유지합니다.

## 재검수 반영

서브에이전트 재검수 후 다음 내용을 반영했습니다.

- README와 `development-plan.md`의 초기 구현 순서를 상세 Phase 순서와 맞췄습니다.
- `api-spec.md`에 Phase 1~4 dev-only dummy user context 전제를 추가했습니다.
- `changeAlertOption`은 MVP 1차에서 설정값 저장만 하고 실제 변경 알림 생성은 후순위임을 명시했습니다.
- README의 예상 구조를 현재 문서 분리 결과와 맞췄습니다.
- `project-brief.md`의 구현 기준 문서 우선순위를 개별 문서 기준으로 명확히 했습니다.

## 추가 문서 검토 반영

서브에이전트의 누락 문서 검토 후 다음 문서를 추가했습니다.

- `docs/task/001_project_bootstrap/issue.md`
- `docs/task/001_project_bootstrap/requirements.md`
- `docs/task/001_project_bootstrap/plan.md`
- `docs/task/001_project_bootstrap/validation_report.md`
- `docs/decisions/README.md`
- `docs/local-development.md`
- `docs/testing-strategy.md`

`local-development.md`와 `testing-strategy.md`는 아직 프로젝트 골격 생성 전이므로 초안 상태입니다. Phase 1 구현 중 실제 실행 명령과 검증 명령으로 보강해야 합니다.

## 최종 문서 검수 반영

마지막 서브에이전트 검수 후 다음 내용을 반영했습니다.

- `architecture.md`와 `development-plan.md`에 Modular Monolith + Layered Architecture 원칙을 명시했습니다.
- `decisions/README.md`에 Gradle/Maven, 패키지명, Node.js 버전, Docker Compose 서비스/DB명, 계층 규칙, `characterImageType` 저장 방식 결정을 추가했습니다.
- `harness-kit-notes.md`를 현재 채택한 경량 구조와 정식 Harness Kit overlay 후보 구조로 분리했습니다.
- `domain-model.md`와 `database-design.md`에 `characterImageType` 관련 내용을 보강했습니다.

## Gemini 수정 후 재검수 반영

Gemini가 Harness Kit overlay 일부를 추가한 뒤, 서브에이전트 검수 결과를 반영했습니다.

- `docs/architecture.md`를 대표 아키텍처 문서로 복구했습니다.
- `docs/project/standards/architecture.md`는 대표 문서를 가리키는 overlay 보조 문서로 낮췄습니다.
- `docs/decisions/README.md`를 대표 결정 기록으로 유지했습니다.
- `docs/project/decisions/README.md`는 대표 결정 기록을 가리키는 overlay 보조 문서로 낮췄습니다.
- `docs/entrypoint.md`의 README 링크를 `../README.md`로 수정했습니다.
- `docs/project/standards/coding_conventions_project.md`의 패키지 구조를 `com.dailyoutfitweather.{module}.{layer}` 기준으로 맞췄습니다.
- 공통 응답 wrapper는 아직 미확정 후보로 낮추고 `docs/api-spec.md`를 우선하도록 정리했습니다.
- Java 21, npm, Session Cookie, Asia/Seoul, 정적 좌표 매핑, `characterImageType` 저장 결정을 대표 결정 기록에 반영했습니다.
