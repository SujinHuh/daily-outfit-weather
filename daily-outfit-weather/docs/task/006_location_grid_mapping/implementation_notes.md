# Implementation Notes: 006 Location Grid Mapping

## 진행 로그

- **2026-05-25:** KMA 격자 좌표 정적 CSV를 추가했다.
- **2026-05-25:** `LocationGridCatalog`와 `/api/locations/search` API를 추가했다.
- **2026-05-25:** 프로필 저장/수정 시 `nx`, `ny`가 없으면 `sido/sigungu/dong`으로 좌표를 자동 보강하도록 했다.
- **2026-05-25:** 좌표 catalog 단위 테스트와 프로필 통합 테스트를 추가했다.
- **2026-06-07:** 현재 KMA grid catalog의 지원 지역과 확장 절차를 `docs/kma-service-area.md`에 문서화했다.

## 구현 중 결정 사항

- MVP catalog에는 현재 테스트/데모 흐름에서 쓰는 `역삼동`, `서초동`, `성수동`, `판교동` 계열을 우선 포함했다.
- 운영 readiness 기준의 지원 지역은 `docs/kma-service-area.md`의 allowlist를 따른다.
- 요청에 `nx`, `ny`가 모두 있으면 catalog보다 요청 값을 우선한다.
- catalog에 없는 위치는 좌표를 null로 유지해 기존 입력 흐름을 깨지 않는다.

## 후속 태스크 후보

- 서비스 claims에 맞춘 KMA grid catalog 확장 및 검증.
- 위치 검색 UI와 연결.
