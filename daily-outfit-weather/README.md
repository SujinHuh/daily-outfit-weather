# 오늘 뭐입지? - Daily Outfit Weather

> 출근 전 오늘의 날씨, 체감온도, 이동 시간대, 개인 민감도를 반영해 옷차림과 준비물을 추천하는 개인화 아침 서비스

## 프로젝트 개요

**오늘 뭐입지?**는 사용자가 아침에 외출하기 전 오늘 입을 옷과 챙길 물건을 빠르게 결정할 수 있도록 돕는 서비스입니다.

일반적인 날씨 앱은 기온, 강수확률, 풍속 같은 정보를 보여주지만, 사용자는 그 정보를 다시 해석해서 “겉옷을 챙길지”, “우산이 필요한지”, “퇴근길에는 추울지”를 직접 판단해야 합니다.

이 프로젝트는 출근 시간, 퇴근 시간, 집/직장 위치, 추위/더위 민감도, 이동수단을 함께 고려해 **오늘의 옷차림과 준비물**을 추천합니다.

## MVP 목표

사용자가 매일 아침 앱을 열었을 때, 출근길과 퇴근길 날씨를 바탕으로 오늘 입을 옷과 챙길 물건을 5초 안에 결정할 수 있게 합니다.

## MVP 최종 포함 기능

- Google OAuth 로그인
- 사용자 온보딩
- 집/직장 위치 등록
- 출근/퇴근/알림 시간 설정
- 추위/더위 민감도 설정
- 기상청 단기예보 API 연동
- 출근/퇴근 시간대 날씨 분석
- 룰 기반 옷차림 추천
- 준비물 추천
- 오늘 추천 결과 저장
- 추천 피드백 등록
- 알림 로그 생성
- React PWA 기본 설정

## 초기 구현 순서

MVP 최종 포함 기능을 한 번에 모두 구현하지 않습니다. 실제 개발은 [개발 순서](docs/development-plan.md)를 기준으로 진행합니다.

초기 구현은 다음 순서로 작게 시작합니다.

1. 인증 없이 로컬/더미 사용자 기준 추천 흐름 구현
2. 사용자/프로필/위치 도메인 구현
3. 추천 엔진과 오늘 추천 API 구현
4. 기상청 API 연결
5. 프론트엔드 MVP 화면 구현
6. Google OAuth 연결
7. 알림 로그와 PWA 설정 확장

## MVP 제외 범위

- 실제 웹 푸시 발송
- FCM 연동
- 미세먼지/자외선 실제 API 연동
- 생리주기 반영
- 가족 프로필
- 옷장 등록
- AI 추천
- 캐릭터 커스터마이징
- 지도 기반 위치 선택
- 현재 위치 자동 감지
- 결제/구독
- 앱스토어/플레이스토어 배포

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Frontend | React, Vite, TypeScript, PWA |
| Backend | Java, Spring Boot, Spring Security, Spring Data JPA |
| Database | PostgreSQL |
| Auth | Google OAuth |
| Weather | 기상청 단기예보 API |
| Process | Harness Kit |

## 예상 구조

```text
daily-outfit-weather
├── backend
│   └── Spring Boot application
├── frontend
│   └── React + Vite + PWA application
├── docs
│   ├── product-requirements.md
│   ├── screen-flow.md
│   ├── domain-model.md
│   ├── database-design.md
│   ├── api-spec.md
│   ├── recommendation-logic.md
│   ├── notification-policy.md
│   ├── architecture.md
│   ├── privacy-policy-notes.md
│   ├── development-plan.md
│   ├── local-development.md
│   ├── testing-strategy.md
│   ├── decisions
│   │   └── README.md
│   ├── entrypoint.md
│   ├── project
│   │   ├── decisions
│   │   │   └── README.md
│   │   └── standards
│   │       ├── architecture.md
│   │       ├── coding_conventions_project.md
│   │       └── testing_profile.md
│   ├── task
│   │   └── 001_project_bootstrap
│   ├── harness-kit-notes.md
│   ├── review-notes.md
│   └── project-brief.md
└── README.md
```

## 아키텍처 요약

```text
[React + Vite + PWA]
        ↓ REST API
[Spring Boot Backend]
        ↓
[PostgreSQL]

외부 연동:
- Google OAuth
- 기상청 단기예보 API
- 추후 Web Push / FCM
```

## 주요 문서

- [문서 진입점](docs/entrypoint.md)
- [제품 요구사항](docs/product-requirements.md)
- [화면 흐름](docs/screen-flow.md)
- [도메인 모델](docs/domain-model.md)
- [DB 설계](docs/database-design.md)
- [API 명세](docs/api-spec.md)
- [추천 로직](docs/recommendation-logic.md)
- [알림 정책](docs/notification-policy.md)
- [아키텍처](docs/architecture.md)
- [개인정보 처리 메모](docs/privacy-policy-notes.md)
- [개발 순서](docs/development-plan.md)
- [로컬 개발 환경](docs/local-development.md)
- [테스트 전략](docs/testing-strategy.md)
- [결정 기록](docs/decisions/README.md)
- [첫 작업 문서](docs/task/001_project_bootstrap/issue.md)
- [Harness Kit 작업 기준](docs/harness-kit-notes.md)
- [문서 검수 메모](docs/review-notes.md)
- [원본 기획/설계 문서](docs/project-brief.md)

## 개발 원칙

- MVP 범위 밖 기능을 먼저 구현하지 않습니다.
- 추천 로직은 컨트롤러나 서비스에 직접 하드코딩하지 않고 별도 엔진으로 분리합니다.
- 알림 생성과 실제 발송 책임을 분리합니다.
- 날씨 API 응답 원본은 저장 가능한 구조로 남깁니다.
- 사용자별 데이터 접근 제어를 지킵니다.
- 프론트엔드는 모바일 사용성을 우선합니다.
- Harness Kit 기준으로 작업 단위, 산출물, 검수 흐름을 문서화합니다.

## 현재 상태

- 프로젝트 폴더 생성 완료
- README 정제 완료
- 원본 기획 문서 `docs/project-brief.md`로 분리 완료
- 경량 개발 계획 작성 완료
- 첫 작업 문서 초안 작성 완료
- 핵심 bootstrap 결정 정리 완료
- Harness Kit overlay는 MVP bootstrap 동안 partial overlay 유지로 결정

## 다음 단계

1. Spring Boot 백엔드 프로젝트 생성
2. React + Vite 프론트엔드 프로젝트 생성
3. PostgreSQL Docker Compose 구성
4. `.env.example` 작성
5. 로컬 실행 문서 보강
