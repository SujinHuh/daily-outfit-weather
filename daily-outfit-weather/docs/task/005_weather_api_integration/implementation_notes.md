# Implementation Notes: 005 Weather API Integration

## 진행 로그

- **2026-05-25:** Phase 5 문서 작성.
- **2026-05-25:** `WeatherApiClient`, `KmaWeatherApiClient`, 기상청 응답 parser, 발표시각 계산기를 추가했다.
- **2026-05-25:** `WeatherSnapshotProvider`를 집/직장 격자 좌표 기반 KMA 호출로 연결했다.
- **2026-05-25:** API 키/좌표/API 장애 시 기본 스냅샷으로 폴백하도록 했다.
- **2026-05-25:** weather parser/base time 단위 테스트와 추천 서비스 연동 테스트를 추가했다.
- **2026-05-25:** 서브 에이전트 검수 반영. KMA timeout, target forecast time 기준 base time 선택, 다음 정시 forecast 조회, encoded service key 문서화, client-level 테스트, fallback 테스트를 추가했다.
- **2026-05-25:** Gimin 심층 검수 반영. 00:00~00:10 base time 날짜 보정, PTY 5/6/7 매핑, fallback warn 로그, 부분 fallback, null 방어 테스트를 추가했다.

## 구현 중 결정 사항

- 단기예보 발표시각은 02/05/08/11/14/17/20/23시 기준이며, target forecast time을 포함할 수 있고 현재 시점에 사용 가능한 가장 적절한 발표시각을 사용한다.
- 출근길 날씨는 HOME 좌표, 퇴근길 날씨는 WORK 좌표를 사용한다.
- 출퇴근 시간이 정각이 아니면 다음 정시 forecast를 사용한다.
- `KMA_SERVICE_KEY`는 공공데이터포털 Encoding 인증키를 사용한다.
- `PTY` 1/4는 비, 2/3은 눈으로 매핑한다.
- 풍속 4.0m/s 이상은 체감온도 -2도, 2.0m/s 이상은 -1도로 계산한다.

## 후속 태스크 후보

- 위치 검색 정적 데이터와 `nx`, `ny` 자동 매핑.
- WeatherForecast 저장/캐시.
- 실제 API 키 기반 수동 smoke test 결과 기록.
