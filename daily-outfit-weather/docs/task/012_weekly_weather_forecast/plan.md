# Task 012 Plan: Weekly Weather Forecast & Outfit Outlook (주간 날씨 및 옷차림 예보)

## 1. 개요 및 세분화 목표

본 플랜은 `Task 012: 주간 날씨 및 옷차림 예보` 신규 기능을 독립적이고 세분화된 4개의 하위 작업(Sub-tasks)으로 분할하여 에이전트 위임 및 단계별 구현/검증을 수행하는 로드맵 문서입니다.

---

## 2. 사전 영향도 파악 완료 체크리스트 (`docs/impact-analysis-guide.md` 이행)

- [x] **DB/역직렬화**: 기존 `saved_recommendations` 스키마 100% 보존. 주간 예보 DTO Null-safe default 파싱 적용.
- [x] **도메인 계산 엔진**: 기존 단일일 룰 엔진(`RecommendationRuleEngine`) 격리 보존 및 7일 일별 보정 확장.
- [x] **API 계약**: `GET /api/recommendations/weekly` 엔드포인트 신설 (`GET /api/recommendations/today` 영향 없음).
- [x] **인증/세션**: 기존 임시 로그인(`1234`, 30d 쿠키) 및 OAuth 유지 (`SecurityConfig` 허용).
- [x] **프론트 UI**: 메인 화면 `Today` 탭 하단 수평 스크롤(CSS Scroll Snap) 카드 UI 추가 (백화 방지 Optional Chaining).
- [x] **외부 API**: 기상청 단기/중기예보 데이터 7일 집계 및 응답 실패 시 `getOrDefault` Fallback 제공.
- [x] **배포/환경**: 기존 `.env` 설정(`APP_TEMP_LOGIN_PASSWORD=1234`) 유지.

---

## 3. 서브 에이전트 위임 세부 작업 분할 (Sub-Task Breakdown)

### 🧩 Sub-Task 1: Backend Domain & Weekly Forecast Engine 구현
- **담당 수행**: Backend Engine & Service Agent
- **작업 내용**:
  1. `WeeklyRecommendationResponse.java`, `DailyForecastSummary.java` DTO 생성
  2. `WeeklyForecastAggregator.java` (7일 날씨 데이터 및 fallback 파싱) 구현
  3. `WeeklyOutfitEngine.java` (기온 구간/강수확률 기반 요일별 옷차림 태그 칩 생성) 구현
  4. `RecommendationService.java`에 `getWeeklyRecommendation(...)` 메서드 추가
  5. 단위 테스트 (`WeeklyOutfitEngineTest`, `WeeklyForecastAggregatorTest`) 작성

### 🧩 Sub-Task 2: Backend Controller, Security & Integration Test
- **담당 수행**: Backend Controller & Security Agent
- **작업 내용**:
  1. `RecommendationController.java`에 `GET /api/recommendations/weekly` 엔드포인트 추가
  2. `SecurityConfig.java`에 `/api/recommendations/weekly` 인증 권한 허용 등록
  3. `RecommendationControllerIntegrationTest.java` 작성 및 통합 테스트 검증

### 🧩 Sub-Task 3: Frontend UI Component & API Integration
- **담당 수행**: Frontend UI Agent
- **작업 내용**:
  1. `App.tsx` 내 `WeeklyRecommendationResponse` 타입 선언 및 API 클라이언트 함수 추가
  2. `Today` 탭 하단에 `WeeklyForecastCarousel` 수평 스크롤 UI 컴포넌트 추가
  3. 요일별 카드(날짜, 날씨 아이콘, 최저/최고기온, 강수확률, 옷차림 태그 칩) 렌더링
  4. `App.css`에 가로 스크롤 snap 및 카드/칩 스타일 추가
  5. 로딩 스켈레톤 UI 및 Error Boundary 예외 처리 적용

### 🧩 Sub-Task 4: Documentation & Final Verification
- **담당 수행**: Documentation & Audit Subagent
- **작업 내용**:
  1. `docs/api-spec.md`에 `GET /api/recommendations/weekly` 명세 작성
  2. `docs/task/012_weekly_weather_forecast/` 내 `implementation_notes.md`, `validation_report.md`, `phase_status.md` 작성
  3. 서브에이전트(Subagent) 교차 검수 수행 (ALL PASS 확인)

---

## 4. 진행 현황 (Phase Status)

- [x] Step 1. 세부 계획 및 사전 영향도 파악 (`plan.md`)
- [ ] Step 2. Backend Domain & Engine 구현 (Sub-Task 1)
- [ ] Step 3. Backend Controller & Security 구현 (Sub-Task 2)
- [ ] Step 4. Frontend UI & API 구현 (Sub-Task 3)
- [ ] Step 5. Documentation & 서브에이전트 전체 검수 (Sub-Task 4)
