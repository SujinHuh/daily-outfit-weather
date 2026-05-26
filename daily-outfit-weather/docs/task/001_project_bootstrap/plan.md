# Plan: 001 Project Bootstrap

## 작업 순서

1. **Backend 초기화:** `backend` 디렉터리에 Gradle 기반 Spring Boot 3.x 프로젝트 생성
2. **Frontend 초기화:** `frontend` 디렉터리에 React + Vite + TypeScript 프로젝트 생성
3. **Infrastructure 구성:** 루트에 `docker-compose.yml` (PostgreSQL) 생성
4. **환경 설정:** `.env.example` 작성 및 백엔드 `application.yml` 연동
5. **문서화:** `README.md`, `docs/local-development.md`, `docs/testing-strategy.md` 갱신
6. **검증:** 백엔드 테스트, 프론트엔드 빌드, Docker Compose 설정 검증 수행

## 확정 기술 구성

- Backend: Java 21, Spring Boot 3.x, Gradle, Spring Web, Spring Data JPA, Flyway, Validation, PostgreSQL Driver
- Frontend: React, Vite, TypeScript, npm, Node.js 24 LTS
- Database: PostgreSQL 16 Docker Compose
- Test DB: Testcontainers PostgreSQL

## 테스트 계획

- **Backend:** Spring Boot Test (`@SpringBootTest`)를 사용하여 애플리케이션 컨텍스트 로딩 확인.
- **Frontend:** 초기 빌드 테스트 (`npm run build`) 수행.
- **Infrastructure:** `docker compose`를 통한 컨테이너 정상 기동 여부 확인.

## 문서 반영 계획

- `README.md`: 프로젝트 개요, 기술 스택, 빠른 실행 방법 업데이트.
- `docs/local-development.md`: 상세 환경 구축 및 트러블슈팅 가이드 작성.
- `docs/testing-strategy.md`: 초기 테스트 실행 방법 명시.

## 비범위 (Non-scope)

- 실제 비즈니스 로직(추천 엔진 등) 구현
- Google OAuth Client ID 설정 및 연동
- 기상청 API 실제 호출 및 파싱
- PWA 상세 설정 (Manifest, Service Worker 등)

## 리스크 관리

- **Dependency Issue:** Spring Boot 3.x와 Java 21, Gradle wrapper 호환성 확인 필요.
- **Node.js 버전:** 로컬 환경의 Node.js 버전이 요구사항(24+)과 맞는지 확인 필수.
