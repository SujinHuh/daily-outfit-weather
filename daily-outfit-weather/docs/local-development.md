# Local Development

로컬 개발 환경 문서입니다.

## 구성

- Backend: Spring Boot 3.x
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

`nvm`을 사용하지 않는 환경에서는 Homebrew의 `node@24`를 사용할 수 있습니다.

```bash
brew install node@24
export PATH="/opt/homebrew/opt/node@24/bin:$PATH"
node -v
```

## 환경변수

기본값:

```text
POSTGRES_DB=daily_outfit_weather
POSTGRES_USER=daily_outfit_weather
POSTGRES_PASSWORD=daily_outfit_weather
POSTGRES_PORT=5432
DATABASE_URL=jdbc:postgresql://localhost:5432/daily_outfit_weather
SERVER_PORT=8080
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
KMA_SERVICE_KEY=
```

## 실행 명령

```bash
# database
cp .env.example .env
docker compose up -d

# backend
cd backend
./gradlew bootRun

# frontend
cd frontend
npm install
npm run dev
```

## 검증 명령

```bash
cd backend
./gradlew test

cd frontend
npm run build
```

## 현재 주의사항

- Node.js는 `.nvmrc` 기준 Node.js 24를 사용합니다.
- 현재 작업 환경이 Node.js 24가 아니면 frontend build가 실패할 수 있습니다.
- DB 통합 테스트는 Testcontainers PostgreSQL을 기준으로 확장합니다.
