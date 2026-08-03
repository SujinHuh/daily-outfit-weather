# Feature Development Trigger & Impact Analysis Rule

Daily Outfit Weather 프로젝트에서 **새로운 기능을 개발하거나 기존 코드를 수정할 때 적용되는 필수 작동 규칙(Trigger Rule)**을 정의합니다.

---

## ⚡ 핵심 작동 규칙 (Core Trigger Rule)

어떠한 신규 기능 추가 또는 변경 작업이 할당되더라도, **개발자 및 AI 에이전트는 코드 작성을 시작하기 '전'에 아래의 3단계 필수 트리거를 이행**해야 합니다.

### Step 1. 트리거 실행: 영향도 분석 지침서 조회 (Mandatory Read)
- **명령**: `view_file` -> [`docs/impact-analysis-guide.md`](../../impact-analysis-guide.md)
- **목적**: 7대 사전 영향도 검토 영역(DB 역직렬화, 계산 엔진, API 계약, 인증/세션, 프론트 UI, 외부 API, 배포 환경) 체크리스트 확인.

### Step 2. 사전 영향도 평가 및 대비책 수립 (Pre-Implementation Analysis)
- 기존 DB 데이터 파손 여부, 역직렬화 기본값(Default fallback) 필요 여부 파악.
- API 스펙 변경에 따른 프론트엔드 타입 파손 여부 확인.
- 파악된 위험 요소에 대한 하위 호환성 유지 로직(예: `refreshIfMissing...`) 사전 작성.

### Step 3. 구현 및 서브에이전트(Subagent) 교차 검수 (Implementation & Audit)
- 작성된 예방 대책을 바탕으로 백엔드/프론트엔드 구현 및 테스트 코드 작성.
- 작업 완료 후 서브에이전트(Subagent)를 실행하여 사전 영향도 평가 결과와의 정합성 교차 검수 진행.

---

## 📌 적용 대상 문서 및 맵핑

본 트리거 규칙은 프로젝트 전체 문서 체계에 일괄 연동되어 있습니다.
- [Project Entrypoint](../../entrypoint.md#⚡-mandatory-feature-development-trigger-rule)
- [Development Plan](../../development-plan.md#⚠️-필수-개발-트리거-사전-영향도-분석-pre-implementation-trigger)
- [Coding Conventions](./coding_conventions_project.md#3-공통-규칙)
- [Harness Kit Notes](../../harness-kit-notes.md#현재-운영-기준)
- [Project README](../../../README.md#개발-원칙)
