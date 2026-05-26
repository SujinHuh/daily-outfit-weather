# Plan: 006 Location Grid Mapping

## 작업 순서

1. KMA 격자 좌표 정적 CSV를 추가한다.
2. CSV를 로드하는 `LocationGridCatalog`를 추가한다.
3. 위치 검색 API를 추가한다.
4. 프로필 저장/수정 로직에서 `nx`, `ny`가 없으면 catalog로 자동 보강한다.
5. 단위 테스트와 통합 테스트를 실행한다.

## 결정

- MVP 검증에 필요한 주요 위치부터 정적 catalog에 포함한다.
- 요청에 `nx`, `ny`가 모두 있으면 요청 값을 우선한다.
- catalog에 없는 위치는 기존처럼 nullable 좌표로 저장한다.
