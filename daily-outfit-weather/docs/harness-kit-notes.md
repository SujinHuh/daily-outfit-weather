# Harness Kit Notes

이 문서는 `daily-outfit-weather` 프로젝트에서 Harness Kit를 어떻게 사용할지 정리합니다.

## 적용 목적

Harness Kit는 구현을 바로 시작하기 전에 작업 단위, 산출물, 검수 기준을 명확히 하기 위해 사용합니다.

이 프로젝트에서는 다음 목적에 맞춰 사용합니다.

- 기능 구현 전 요구사항과 범위 확정
- 작업 단위별 계획/구현/검증 기록
- MVP 범위 밖 기능 유입 방지
- 서브에이전트를 통한 감사/검수 흐름 분리
- 반복 세션에서도 같은 기준으로 개발 지속

## 현재 판단

현재 폴더는 새 프로젝트에 가깝지만 이미 README와 기획 문서가 존재합니다. 따라서 완전한 빈 프로젝트 bootstrap보다는 **기존 프로젝트 첫 도입** 방식으로 Harness Kit를 붙이는 것이 적절합니다.

Harness Kit의 quickstart 기준으로는 다음 경로가 맞습니다.

```text
기존 프로젝트 첫 도입
→ adopt dry-run
→ safe write 또는 수동 문서 반영
→ overlay consistency 검증
→ task workspace 생성
```

## 현재 채택 구조

MVP bootstrap 동안은 정식 Harness Kit overlay를 전부 도입하지 않고, 일부 구조만 도입한 **partial overlay** 상태를 유지합니다.

대표 source-of-truth는 기존 경량 문서입니다. `docs/project/*` 아래 문서는 Harness Kit overlay용 보조 진입점입니다.

## 현재 운영 기준

MVP bootstrap 기간에는 partial overlay를 공식 운영 기준으로 사용합니다.

- canonical 문서: `docs/architecture.md`, `docs/decisions/README.md`, `docs/testing-strategy.md`, `docs/impact-analysis-guide.md`
- overlay 보조 문서: `docs/project/*`
- task workspace: `docs/task/<task_id>`
- **필수 사전 트리거**: 모든 구현 작업은 코드 작성 전 `docs/impact-analysis-guide.md`를 조회(`view_file`)하여 7대 사전 영향도 평가 및 예방 대책을 수립한 후 Task를 진행합니다.
- 모든 구현 작업은 task workspace의 `issue.md`, `requirements.md`, `plan.md`, `phase_status.md`, `implementation_notes.md`, `validation_report.md`를 기준으로 진행합니다.
- 정식 Harness Kit overlay 후보 구조는 참고용이며, 현재 작업 기준이 아닙니다.

```text
docs
├── product-requirements.md
├── screen-flow.md
├── domain-model.md
├── database-design.md
├── api-spec.md
├── recommendation-logic.md
├── notification-policy.md
├── architecture.md
├── privacy-policy-notes.md
├── development-plan.md
├── local-development.md
├── testing-strategy.md
├── decisions
│   └── README.md
├── entrypoint.md
├── project
│   ├── decisions
│   │   └── README.md
│   └── standards
│       ├── architecture.md
│       ├── coding_conventions_project.md
│       └── testing_profile.md
├── task
│   └── 001_project_bootstrap
│       ├── issue.md
│       ├── requirements.md
│       ├── plan.md
│       ├── phase_status.md
│       ├── implementation_notes.md
│       └── validation_report.md
├── harness-kit-notes.md
├── review-notes.md
└── project-brief.md
```

이 구조는 현재 프로젝트의 실제 문서 구조입니다.

대표 문서:

- Architecture: `docs/architecture.md`
- Decisions: `docs/decisions/README.md`
- Testing strategy: `docs/testing-strategy.md`

Overlay 보조 문서:

- `docs/project/standards/architecture.md`
- `docs/project/decisions/README.md`
- `docs/project/standards/testing_profile.md`

## 정식 Harness Kit overlay 도입 시 후보 구조

```text
docs
├── project-brief.md
├── development-plan.md
├── harness-kit-notes.md
├── review-notes.md
├── entrypoint.md
├── project
│   ├── decisions
│   │   └── README.md
│   └── standards
│       ├── architecture.md
│       ├── implementation_order.md
│       ├── coding_conventions_project.md
│       ├── quality_gate_profile.md
│       └── testing_profile.md
└── task
    └── <task_id>
        ├── issue.md
        ├── requirements.md
        ├── plan.md
        ├── phase_status.md
        ├── implementation_notes.md
        └── validation_report.md
```

위 구조는 지금 바로 채택한 구조가 아니라, 정식 Harness Kit overlay를 도입할 때의 후보 구조입니다.

## Phase 운영 방식

각 작업은 Harness Kit의 Phase 흐름을 따릅니다.

1. Phase 1: 요구사항과 계획 정리
2. Phase 2: 테스트와 구현
3. Phase 3: 통합
4. Phase 4: 검증
5. Phase 5: 문서화

가벼운 작업도 task workspace의 기본 6개 문서(`issue.md`, `requirements.md`, `plan.md`, `phase_status.md`, `implementation_notes.md`, `validation_report.md`)를 유지합니다. 다만 인증, 추천 엔진, 외부 API 연동처럼 위험도가 높은 작업은 Phase별 감사와 검증 기록을 더 상세히 남깁니다.

## 첫 번째 작업 제안

첫 작업은 프로젝트 생성 자체입니다.

작업 ID:

```text
001_project_bootstrap
```

범위:

- Spring Boot 백엔드 프로젝트 생성
- React + Vite 프론트엔드 프로젝트 생성
- PostgreSQL Docker Compose 구성
- `.env.example` 작성
- README 실행 방법 보강

비범위:

- Google OAuth 실제 연동
- 기상청 API 실제 호출
- 추천 엔진 구현
- PWA 상세 설정

검증:

- 백엔드 기본 테스트 통과
- 프론트엔드 빌드 통과
- PostgreSQL 컨테이너 실행 확인
- README 실행 명령 확인

## 서브에이전트 검수 기준

서브에이전트는 구현자가 작성한 문서와 코드에 대해 다음을 확인합니다.

- README가 너무 장황하지 않은가
- 문서 간 프로젝트 이름과 MVP 범위가 일관적인가
- Harness Kit 기준 작업 단위가 명확한가
- 현재 Phase에서 하지 말아야 할 기능이 섞이지 않았는가
- 실행/검증 방법이 개발자가 따라 할 수 있을 만큼 구체적인가
- 외부 API, OAuth, DB 같은 위험 요소가 환경변수와 실패 처리까지 고려되어 있는가

## 아직 해야 할 일

- Harness validator 실행 방식 확정

완료된 일:

- `docs/task/001_project_bootstrap/*` 경량 작업 문서 생성
- `docs/decisions/README.md` 생성
- `docs/entrypoint.md` 생성
- `docs/project/standards/*` 일부 생성
- MVP bootstrap 기간에는 Harness Kit partial overlay 유지로 결정
- `001_project_bootstrap` task workspace 기본 6개 문서 구성 완료
