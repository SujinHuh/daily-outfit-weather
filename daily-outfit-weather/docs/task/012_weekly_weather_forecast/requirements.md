# Task 012 Requirements: Weekly Weather Forecast & Outfit Outlook (주간 날씨 및 옷차림 예보)

## 1. 개요 및 배경

사용자가 오늘 하루 날씨뿐만 아니라, **이번 주(7일간)의 날씨 흐름과 요일별 옷차림 가이드**를 한눈에 파악하여 주간 일정(출장, 외출, 주말 야외활동 등) 및 옷차림을 사전에 준비할 수 있도록 메인 화면 하단에 **7일간의 주간 날씨 & 옷차림 카셀(Carousel) / 카드 UI**를 구현합니다.

---

## 2. 사전 영향도 분석 (Pre-Implementation Impact Analysis)

`docs/impact-analysis-guide.md` 규정에 따라 사전 영향도를 평가함:

| 검토 영역 | 영향 파악 | 대비책 & 예방 로직 |
| --- | --- | --- |
| **1. DB/역직렬화** | 기존 `saved_recommendations` 스키마 영향 없음 | 주간 예보 캐시용 `weekly_forecast_cache` 테이블 또는 In-Memory 캐시 사용시 JSON Null-safe default fallback 적용 |
| **2. 계산 엔진** | 기존 단일일 룰 엔진(`RecommendationRuleEngine`) 재활용 | D+3~D+6 중기예보 구간 습도 미제공 시 기본 최저/최고기온 기반 체감온도 fallback 적용 |
| **3. API 계약** | 신규 API 엔드포인트 신설 (`GET /api/recommendations/weekly`) | 기존 `GET /api/recommendations/today` 스펙 파손 없음, `docs/api-spec.md` 동시 갱신 |
| **4. 인증/세션** | 기존 임시 로그인(`1234`, 30d 쿠키) 및 OAuth 유지 | `SecurityConfig`에 `/api/recommendations/weekly` 세션 인증 등록 |
| **5. 프론트 UI** | 기존 메인 화면 `Today` 탭 하단에 가로 스크롤 카드 추가 | 모바일 수평 스크롤 UI(Touch Swipe, CSS Snap), 로딩 스켈레톤, 널 안전 파싱 (`day?.outfitSummary ?? '-'`) 적용 |
| **6. 외부 API** | 기상청 단기예보(격자 좌표 `nx, ny`) + 중기예보(지역코드 `regId`) 결합 | 외부 API 응답 지연/누락 시 기본 예보 데이터 및 `getOrDefault` Fallback 제공 |
| **7. 배포/환경** | 신규 환경변수 추가 필요 시 `.env.example` 갱신 | GCP 임시 로그인 설정(`1234`, 30d 유효) 유지하며 배포 |

---

## 3. 세부 기능 요구사항 (Requirements)

### 3.1. 백엔드 (Backend API & Domain)
1. **API 엔드포인트 신설 및 보안 설정**:
   - `GET /api/recommendations/weekly`
   - `SecurityConfig`에 인증 등록하여 임시 로그인(`1234`, 30d 쿠키) 및 OAuth 세션으로 조회 허용.
   - `docs/api-spec.md` 동시 업데이트.
2. **날씨 데이터 집계 및 기상청 API 파싱 (Forecast Aggregator)**:
   - D+0 ~ D+2 (3일간): 기상청 단기예보(격자 좌표 `nx, ny`) 기반 최저/최고기온, 습도, 강수확률, 대표 날씨 아이콘 계산.
   - D+3 ~ D+6 (4일간): 기상청 중기예보(행정구역 `regId` 매핑) 기반 최저/최고기온 및 대표 날씨 상태 계산 (습도 미제공 시 기본 기온 fallback 적용).
3. **주간 요약 옷차림 키워드 생성 (Weekly Outfit Summary)**:
   - 각 요일의 최저/최고 체감온도를 기준으로 대표 옷차림 칩 생성 (예: `[반팔 + 얇은 셔츠]`, `[니트 + 자켓]`, `[우산 필수]`, `[두꺼운 코트]` 등).

### 3.2. 프론트엔드 (Frontend UI/UX)
1. **메인 화면 `Today` 탭 하단 7일 카셀 카드**:
   - 기존 메인 탭(`Today`, `Commute`, `Outfit`, `Items`, `Feedback`) 중 **`Today` 탭 하단**에 **"이번 주 날씨 & 옷차림 흐름"** 섹션 배치.
   - 모바일 사용성을 고려한 **가로 스크롤(Horizontal Swipeable Cards with CSS Scroll Snap)** 형태로 배치.
2. **요일별 카드 표시 정보**:
   - 요일 및 날짜 (예: `오늘 8/1(토)`, `내일 8/2(일)`, `월 8/3` ...)
   - 날씨 상태 아이콘 (맑음, 구름많음, 비, 강풍 등)
   - 최저/최고 기온 및 강수확률 (예: `21° / 29° · ☔60%`)
   - 요일별 추천 옷차림 태그 칩 (예: `[반팔 + 얇은 셔츠]`, `[우산 필수]`)
3. **로딩 스켈레톤 및 예외 처리**:
   - 주간 데이터 로딩 중에는 카셀 Skeleton UI 표시.
   - 데이터 로드 실패 시 재시도 버튼 및 Error Boundary 렌더링.

---

## 4. 검증 및 산출물 기준 (Acceptance Criteria)

1. `GET /api/recommendations/weekly` 호출 시 오늘부터 7일간의 날씨 및 옷차림 키워드가 JSON으로 정상 반환되는가?
2. `docs/api-spec.md`에 `GET /api/recommendations/weekly` 명세가 업데이트되었는가?
3. 모바일 브라우저 `Today` 탭 하단에서 7일간의 날씨 카드가 깨짐 없이 가로 스크롤로 보여지는가?
4. 기존 오늘 추천 기능(`GET /api/recommendations/today`) 및 임시 로그인(`1234`, 30일 유지) 기능에 전혀 영향이 없는가?
5. 백엔드 단위/통합 테스트 (`./gradlew test`) 및 프론트엔드 빌드 (`npm run lint && npm run build`)가 통과하는가?
6. 작업 완료 후 서브에이전트(Subagent) 교차 검수를 완료했는가?
