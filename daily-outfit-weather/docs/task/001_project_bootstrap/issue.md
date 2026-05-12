# Issue: 001 Project Bootstrap

## 목적

`daily-outfit-weather`의 실제 구현을 시작할 수 있도록 백엔드, 프론트엔드, DB, 로컬 개발 환경의 기본 골격을 만든다.

## 배경

현재 프로젝트는 문서 구조만 준비되어 있고 실제 구현 파일은 없다.

첫 작업에서는 기능 구현보다 실행 가능한 프로젝트 골격을 만드는 데 집중한다.

## 범위

- Spring Boot 백엔드 프로젝트 생성
- React + Vite + TypeScript 프론트엔드 프로젝트 생성
- PostgreSQL Docker Compose 구성
- `.env.example` 작성
- 로컬 실행 문서 보강
- 기본 테스트/빌드 명령 확인

## 비범위

- Google OAuth 실제 연동
- 기상청 API 실제 호출
- 추천 엔진 구현
- 오늘 추천 API 구현
- 피드백 API 구현
- 실제 푸시 알림 구현
- PWA 상세 설정

## 완료 기준

- 백엔드 애플리케이션이 로컬에서 실행 가능하다.
- 프론트엔드 애플리케이션이 로컬에서 실행 가능하다.
- PostgreSQL 컨테이너 실행 구성이 존재한다.
- 환경변수 예시가 존재한다.
- README 또는 `docs/local-development.md`에 실행 방법이 기록된다.
- 기본 검증 명령이 문서화된다.
