# Plan: 005 Weather API Integration

## 작업 순서

1. 기상청 단기예보 client 인터페이스와 구현체를 추가한다.
2. 발표일자/발표시각 계산기를 추가한다.
3. 기상청 카테고리를 `WeatherSnapshot`으로 변환하는 parser를 추가한다.
4. `WeatherSnapshotProvider`에서 집/직장 좌표를 기준으로 client를 호출한다.
5. API 키/좌표/API 장애 시 기본 스냅샷으로 폴백한다.
6. parser, base time, 추천 연동 단위 테스트를 실행한다.

## 결정

- 단기예보 API의 `TMP`, `POP`, `PTY`, `WSD`만 MVP 입력으로 사용한다.
- 체감온도는 내부 계산값으로 유지한다.
- 실제 외부 API 호출은 기본 테스트에 포함하지 않는다.
