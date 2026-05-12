# Local Development

로컬 개발 환경 문서입니다. 현재는 프로젝트 골격 생성 전 초안이며, `001_project_bootstrap` 작업에서 실제 명령으로 보강합니다.

## 예정 구성

- Backend: Spring Boot
- Build Tool: Gradle
- Frontend: React + Vite + TypeScript
- Database: PostgreSQL Docker Compose

## 필요한 도구

확정:

- Java 21
- Node.js 24 LTS
- Gradle
- npm
- Docker 또는 Docker Desktop

Node.js 버전은 루트 `.nvmrc`의 `24`를 기준으로 맞춥니다.

```bash
nvm install
nvm use
node -v
```

`node -v`는 `v24.x.x`가 나와야 합니다.

## 환경변수

`.env.example` 생성 후 이 문서를 업데이트합니다.

기본값:

```text
POSTGRES_DB=daily_outfit_weather
POSTGRES_USER=daily_outfit_weather
POSTGRES_PASSWORD=daily_outfit_weather
POSTGRES_PORT=5432
DATABASE_URL=jdbc:postgresql://localhost:5432/daily_outfit_weather
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
KMA_SERVICE_KEY=
```

## 실행 명령

Phase 1 bootstrap 완료 전까지는 아래 명령을 목표 실행 명령으로 둡니다.
구현 후 실제 동작 여부를 검증하고 이 문서를 갱신합니다.

```bash
# database
docker compose up -d

# backend
cd backend
./gradlew bootRun

# frontend
cd frontend
npm run dev
```

## 검증 명령

Phase 1 bootstrap 완료 전까지는 아래 명령을 목표 검증 명령으로 둡니다.
구현 후 실제 동작 여부를 검증하고 이 문서를 갱신합니다.

```bash
cd backend
./gradlew test

cd frontend
npm run build
```
