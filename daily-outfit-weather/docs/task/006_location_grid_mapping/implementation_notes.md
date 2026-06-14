# Implementation Notes: 006 Location Grid Mapping

## 진행 로그

- **2026-05-25:** KMA 격자 좌표 정적 CSV를 추가했다.
- **2026-05-25:** `LocationGridCatalog`와 `/api/locations/search` API를 추가했다.
- **2026-05-25:** 프로필 저장/수정 시 `nx`, `ny`가 없으면 `sido/sigungu/dong`으로 좌표를 자동 보강하도록 했다.
- **2026-05-25:** 좌표 catalog 단위 테스트와 프로필 통합 테스트를 추가했다.
- **2026-06-07:** 현재 KMA grid catalog의 지원 지역과 확장 절차를 `docs/kma-service-area.md`에 문서화했다.
- **2026-06-14:** 샘플 allowlist CSV를 전국 행정동 GeoJSON 기반 생성 CSV로 교체했다.
- **2026-06-14:** `scripts/generate-kma-location-grids.mjs`를 추가해 행정동 중심 좌표를 KMA DFS `nx`, `ny`로 변환하는 재생성 절차를 남겼다.
- **2026-06-14:** `성내동`, `잠실동`처럼 기본 동 이름으로 검색해도 `성내1동`, `잠실본동` 등 세부 행정동이 검색되도록 alias matching을 추가했다.

## 구현 중 결정 사항

- catalog는 `backend/src/main/resources/location/kma_location_grids.csv`를 기준으로 한다.
- 현재 catalog는 3,495개 행정동/읍면동을 포함한다.
- 좌표는 행정동 GeoJSON 중심 좌표를 기상청 DFS 격자로 변환해 생성한다.
- 요청에 `nx`, `ny`가 모두 있으면 catalog보다 요청 값을 우선한다.
- catalog에 없는 위치는 좌표를 null로 유지해 기존 입력 흐름을 깨지 않는다.

## 후속 태스크 후보

- 공식/승인된 KMA per-dong grid source가 확보되면 현재 centroid 기반 catalog와 대조 검증.
- 주요 출시 지역에 대해 실제 KMA API live smoke 검증.
- 위치 검색 UI와 연결.
