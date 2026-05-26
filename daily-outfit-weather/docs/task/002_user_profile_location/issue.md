# Issue: 002 User Profile Location

## 목표

추천 생성을 위한 사용자 설정, 프로필, 집/직장 위치를 저장하고 조회할 수 있게 한다.

## 범위

- User, UserProfile, Location 도메인 생성
- 온보딩 저장 API 구현
- 프로필 조회/수정 API 구현
- MVP 기준 dev-only dummy user context 사용
- 위치 입력은 동 단위 수동 입력으로 시작

## 비범위

- Google OAuth 실제 연동
- 지도/현재 위치 기반 위치 선택
- 기상청 좌표 매핑 정적 데이터 검색
- 추천 생성 로직
