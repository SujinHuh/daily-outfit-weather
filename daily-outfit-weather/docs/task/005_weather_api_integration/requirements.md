# Requirements: 005 Weather API Integration

## 목표

- 기상청 단기예보 API를 이용해 출근/퇴근 시간대 날씨를 가져온다.
- 추천 엔진 입력 DTO인 `WeatherSnapshot`으로 기상청 응답을 변환한다.
- 외부 API 장애나 설정 누락 시 오늘 추천 API가 실패하지 않도록 기본 스냅샷으로 폴백한다.

## 범위

- 기상청 `getVilageFcst` client
- 발표일자/발표시각 계산
- `TMP`, `POP`, `PTY`, `WSD` 카테고리 파싱
- 체감온도 내부 계산
- 집/직장 격자 좌표 기반 출근/퇴근 날씨 조회

## 비범위

- 위치 검색 정적 데이터 구축
- WeatherForecast DB 저장
- 미세먼지/자외선
- 실제 공공데이터포털 API 키를 사용하는 라이브 통합 테스트
