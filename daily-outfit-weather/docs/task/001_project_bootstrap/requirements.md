# Requirements: 001 Project Bootstrap

## 기능 요구사항

- `backend` 디렉터리에 Spring Boot 프로젝트를 생성한다.
- `frontend` 디렉터리에 React + Vite + TypeScript 프로젝트를 생성한다.
- `docker-compose.yml`로 PostgreSQL을 실행할 수 있게 한다.
- `.env.example`에 로컬 개발에 필요한 환경변수 후보를 정리한다.
- 로컬 실행 문서를 작성하거나 README를 보강한다.

## 품질 요구사항

- 생성된 프로젝트는 로컬에서 실행 가능한 상태여야 한다.
- 실행 명령은 개발자가 그대로 따라 할 수 있어야 한다.
- 실제 비밀값은 저장소에 커밋하지 않는다.
- 기능 구현은 하지 않고 프로젝트 골격만 만든다.

## 제약사항

- MVP 1차 구현 순서를 따른다.
- 인증과 외부 API 연동은 이번 작업에서 제외한다.
- Harness Kit overlay는 이번 작업에서 강제 도입하지 않는다.

## 검증 요구사항

- 백엔드 기본 테스트 또는 애플리케이션 부팅 검증
- 프론트엔드 빌드 또는 타입 체크 검증
- PostgreSQL 컨테이너 실행 검증
- 문서 링크와 실행 명령 확인
