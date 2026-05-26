# 오늘 뭐입지? - Daily Outfit Weather

출근 전 오늘의 날씨, 체감온도, 이동 시간대, 개인 민감도를 반영해 옷차림과 준비물을 추천하는 개인화 아침 서비스입니다.

## 프로젝트 위치

현재 구현 프로젝트는 [`daily-outfit-weather`](daily-outfit-weather) 디렉터리에서 관리합니다.

- [프로젝트 README](daily-outfit-weather/README.md)
- [문서 진입점](daily-outfit-weather/docs/entrypoint.md)
- [제품 요구사항](daily-outfit-weather/docs/product-requirements.md)
- [개발 순서](daily-outfit-weather/docs/development-plan.md)
- [결정 기록](daily-outfit-weather/docs/decisions/README.md)
- [Harness Kit 작업 기준](daily-outfit-weather/docs/harness-kit-notes.md)
- [첫 작업: Project Bootstrap](daily-outfit-weather/docs/task/001_project_bootstrap/issue.md)

## MVP 목표

사용자가 매일 아침 앱을 열었을 때, 출근길과 퇴근길 날씨를 바탕으로 오늘 입을 옷과 챙길 물건을 빠르게 결정할 수 있게 합니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Frontend | React, Vite, TypeScript, PWA |
| Backend | Java 21, Spring Boot, Spring Security, Spring Data JPA |
| Database | PostgreSQL |
| Test DB | Testcontainers PostgreSQL |
| Auth | Google OAuth |
| Process | Harness Kit partial overlay |

## 현재 상태

- 기획/설계 문서 정리 완료
- Harness Kit partial overlay 운영 기준 정리 완료
- `001_project_bootstrap` task workspace 구성 완료
- Node.js 24 LTS 기준 고정
- `feature/bootstrap-project-foundation` 브랜치에서 Phase 1 bootstrap 검증 완료

## 다음 단계

1. Phase 1 변경사항 커밋 및 PR 생성
2. Phase 2 사용자/프로필/위치 기본 도메인 작업 브랜치 생성
3. 온보딩 저장/조회 API 구현
