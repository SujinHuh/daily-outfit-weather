# Plan: 001 Project Bootstrap

## 작업 순서

1. 백엔드 프로젝트 생성 방식 결정
2. Spring Boot 프로젝트 생성
3. 프론트엔드 프로젝트 생성 방식 결정
4. React + Vite + TypeScript 프로젝트 생성
5. PostgreSQL Docker Compose 구성
6. `.env.example` 작성
7. `docs/local-development.md` 보강
8. `docs/testing-strategy.md` 보강
9. README 현재 상태 업데이트
10. 기본 실행/빌드/테스트 검증

## 백엔드 확정 기술 구성

- Java 21
- Spring Boot 3.x
- Gradle
- Spring Web
- Spring Data JPA
- Flyway
- Spring Security dependency only
- PostgreSQL Driver
- Validation
- Testcontainers PostgreSQL

OAuth와 실제 Security 세부 설정은 후속 Phase에서 구현한다.

## 프론트엔드 확정 기술 구성

- React
- Vite
- TypeScript
- npm
- Node.js 24 LTS

PWA 상세 설정은 후속 Phase에서 구현한다.

## 검증 계획

- 백엔드 테스트 실행
- 프론트엔드 빌드 실행
- Docker Compose 설정 파일 검토 또는 컨테이너 실행
- README와 로컬 개발 문서의 명령 확인

## 리스크

- 네트워크 제한으로 프로젝트 생성 또는 의존성 설치가 실패할 수 있다.
- Spring Boot 3.x의 정확한 patch 버전 선택이 필요하다.
- 로컬 Node.js가 `.nvmrc`의 Node.js 24와 다르면 프론트엔드 설치/빌드가 실패할 수 있다.

## 확정된 bootstrap 결정

- Java 21
- Gradle
- npm
- Node.js 24 LTS
- Testcontainers PostgreSQL
- Docker Compose service: `postgres`
- Database/user: `daily_outfit_weather`
