# Requirements: 001 Project Bootstrap

## 기능 요구사항

- **Backend Scaffold:** `backend` 디렉터리에 Java 21, Spring Boot 3.x 프로젝트를 생성한다.
- **Frontend Scaffold:** `frontend` 디렉터리에 React + Vite + TypeScript 프로젝트를 생성한다.
- **Infrastructure:** `docker-compose.yml`을 통해 PostgreSQL 16+ 컨테이너를 실행할 수 있게 한다.
- **Environment:** `.env.example`에 로컬 개발에 필요한 환경변수(DB URL, Credential 등)를 정의한다.
- **Documentation:** `README.md` 및 `docs/local-development.md`에 프로젝트 실행 방법을 명시한다.

## 품질 요구사항

- **Buildability:** 백엔드와 프론트엔드 모두 초기 상태에서 빌드 오류가 없어야 한다.
- **Runnability:** Docker Compose 기동 후 백엔드 애플리케이션이 정상적으로 부팅되어야 한다.
- **Safety:** 실제 DB 비밀번호나 API 키 등 민감 정보가 소스 코드나 설정 파일에 직접 포함되지 않아야 한다.

## 제약사항

- **Technology Stack:** Java 21, Gradle, Node.js 24, npm을 사용한다.
- **Harness Kit:** MVP bootstrap 기간에는 partial overlay 운영 기준을 유지한다. Full overlay 런타임/스크립트 도입은 별도 결정 전까지 제외한다.
- **Scope:** 비즈니스 로직 구현, OAuth 연동, 실제 날씨 API 연동은 이번 작업 범위에서 제외한다.

## 검증 요구사항

- **Backend Verification:** `./gradlew test` 또는 `./gradlew build` 성공 확인.
- **Frontend Verification:** `npm run build` 성공 확인.
- **Infrastructure Verification:** `docker compose up -d` 후 PostgreSQL 접속 확인.
- **Doc Verification:** `README.md`의 실행 가이드가 최신 상태인지 확인.
