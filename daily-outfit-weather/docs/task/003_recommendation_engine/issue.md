# Issue: 003 Recommendation Engine

## 목표

외부 날씨 API 없이도 입력 날씨값과 사용자 설정을 기반으로 룰 기반 옷차림 추천을 생성한다.

## 범위

- 추천 입력 DTO 정의
- WeatherConditionAnalyzer 구현
- RecommendationRuleEngine 구현
- OutfitSelector 구현
- ItemSelector 구현
- RecommendationMessageGenerator 구현
- 추천 엔진 단위 테스트 작성

## 비범위

- 오늘 추천 API
- 추천 결과 DB 저장
- 실제 기상청 API 연동
- 사용자 프로필 조회와 추천 엔진 연결
