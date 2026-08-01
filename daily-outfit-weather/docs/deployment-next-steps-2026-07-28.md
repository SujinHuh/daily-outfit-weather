# Deployment Next Steps - 2026-07-28

이 문서는 2026-07-28 기준 배포 전 남은 작업과 오늘 수행한 구현/검증 로그를 정리한다.

## 1. 남은 작업

### 1. 운영 `.env` 실값 준비

운영 배포 전 `daily-outfit-weather/.env` 파일을 만들고 아래 값을 실제 운영 값으로 채운다.

```dotenv
POSTGRES_DB=daily_outfit_weather
POSTGRES_USER=daily_outfit_weather
POSTGRES_PASSWORD=<strong-production-db-password>

SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=80

APP_FRONTEND_SUCCESS_URL=https://<production-domain>
APP_SECURITY_ALLOWED_ORIGINS=https://<production-domain>
APP_NOTIFICATION_GENERATE_DUE_TOKEN=<long-random-internal-job-token>

GOOGLE_CLIENT_ID=<google-oauth-client-id>
GOOGLE_CLIENT_SECRET=<google-oauth-client-secret>
GOOGLE_REDIRECT_URI=https://<production-domain>/login/oauth2/code/google

KMA_SERVICE_KEY=<kma-encoding-service-key>
KMA_BASE_URL=http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0
KMA_CONNECT_TIMEOUT=2s
KMA_READ_TIMEOUT=3s

APP_WEATHER_FALLBACK_ENABLED=true
SESSION_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=lax
```

GCP VM public IP로 HTTP 임시 운영을 한다면 다음 값은 HTTP 기준으로 둔다.

```dotenv
APP_FRONTEND_SUCCESS_URL=http://<GCP_VM_PUBLIC_IP>
APP_SECURITY_ALLOWED_ORIGINS=http://<GCP_VM_PUBLIC_IP>
GOOGLE_REDIRECT_URI=http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google
SESSION_COOKIE_SECURE=false
```

### 2. Google Console OAuth 설정

운영 Google OAuth client에 아래 값을 등록한다.

```text
Authorized JavaScript origins:
https://<production-domain>

Authorized redirect URIs:
https://<production-domain>/login/oauth2/code/google
```

GCP VM public IP로 HTTP 임시 운영을 한다면 아래처럼 등록한다.

```text
Authorized JavaScript origins:
http://<GCP_VM_PUBLIC_IP>

Authorized redirect URIs:
http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google
```

### 3. 운영 서버 배포

운영 서버에서 아래 명령을 실행한다.

```bash
cd /home/sujin941220/Playground/daily-outfit-weather/daily-outfit-weather
docker compose --env-file .env -f docker-compose.prod.yml up -d --build --force-recreate
docker compose --env-file .env -f docker-compose.prod.yml ps
```

### 4. 배포 후 smoke 검증

운영 도메인 기준:

```bash
BASE_URL="https://<production-domain>" scripts/deployment-smoke.sh
curl -i "https://<production-domain>/api/health"
curl -i "https://<production-domain>/oauth2/authorization/google"
docker compose --env-file .env -f docker-compose.prod.yml logs --tail=100 backend
```

로컬 또는 VM 내부 포트 기준:

```bash
BASE_URL="http://localhost:${FRONTEND_PORT:-8080}" scripts/deployment-smoke.sh
curl -i "http://localhost:${FRONTEND_PORT:-8080}/api/health"
curl -i "http://localhost:${FRONTEND_PORT:-8080}/oauth2/authorization/google"
```

### 5. 브라우저 수동 smoke

운영 URL에서 직접 확인한다.

1. Chrome 시크릿 창에서 운영 URL 접속
2. Google 로그인 완료
3. `/api/me`가 사용자 정보를 반환하는지 확인
4. 온보딩 저장
5. 오늘 추천 생성
6. 오늘 추천 화면에서 알림 권한 허용
7. `오늘 알림 보내기` 버튼으로 브라우저 알림 표시 확인
8. Chrome 메뉴에서 앱 설치 가능 여부 확인
9. 설치된 앱에서 새로고침 후 세션 유지 확인
10. 피드백 저장 확인
11. 로그아웃 후 보호 API가 401을 반환하는지 확인

### 6. 자동 Web Push 알림 구현 여부 결정

현재 구현은 앱 안에서 버튼을 눌러 오늘 추천 알림을 즉시 보내는 수준이다.

아직 미구현인 기능:

1. 앱이 닫혀 있어도 설정 시간에 자동으로 뜨는 Web Push
2. `PushManager.subscribe()` 기반 브라우저 push subscription 생성
3. 사용자별 push subscription 저장 API
4. service worker `push` 이벤트 처리
5. VAPID 또는 FCM 기반 백엔드 push 전송기
6. `notification_logs`의 `PENDING -> SENT/FAILED/SKIPPED` 상태 갱신
7. 운영 cron 또는 Spring `@Scheduled` 기반 전송 작업

추천 구현 순서:

1. `push_subscriptions` 테이블과 사용자별 구독 CRUD 추가
2. 프론트에서 알림 권한 허용 후 `PushManager.subscribe()` 호출
3. service worker에 `push` 이벤트 핸들러 추가
4. 백엔드 Web Push 전송 서비스 추가
5. 알림 로그 상태 전이와 실패 사유 저장
6. 운영 스케줄러 연결
7. due 로그 생성, 중복 방지, 전송 성공/실패 테스트 추가

### 7. 안정화 개선 후보

현재 배포 차단은 아니지만 다음 작업으로 권장한다.

1. `generateDueLogs(targetDate, currentTime)`가 오늘이 아닌 날짜로 호출될 때 추천 저장 날짜와 날씨 조회 날짜가 어긋날 수 있는 문제 개선
2. 동시 알림 생성 시 추천 unique key 충돌 가능성 방어
3. 일부 사용자 날씨 조회 실패가 전체 due-log 작업을 롤백하지 않도록 사용자 단위 실패 격리
4. 알림 클릭 시 쿼리/해시가 다른 기존 PWA 창도 재사용하도록 UX 개선

### 8. git 정리

현재 `codex-test.txt`가 staged 추가 상태다. 실제 배포 커밋에 포함할지 결정해야 한다.

배포 기능 변경 파일:

```text
daily-outfit-weather/backend/src/main/java/com/dailyoutfitweather/notification/service/NotificationLogService.java
daily-outfit-weather/backend/src/main/java/com/dailyoutfitweather/notification/service/NotificationRecommendationSummary.java
daily-outfit-weather/backend/src/main/java/com/dailyoutfitweather/recommendation/service/RecommendationService.java
daily-outfit-weather/backend/src/test/java/com/dailyoutfitweather/notification/NotificationLogServiceTest.java
daily-outfit-weather/backend/src/test/java/com/dailyoutfitweather/notification/service/NotificationRecommendationSummaryTest.java
daily-outfit-weather/frontend/src/App.css
daily-outfit-weather/frontend/src/App.tsx
daily-outfit-weather/frontend/vite.config.ts
daily-outfit-weather/frontend/public/notification-sw.js
```

## 2. 2026-07-28 작업 로그

### 1. Git pull

명령:

```bash
git status --short --branch
git remote -v
git pull
git status --short --branch
```

결과:

```text
origin: git@github.com:SujinHuh/daily-outfit-weather.git
main branch fast-forward updated: a90661d..8c482da
new remote branch: origin/수정-배포-추천-스모크-안정화
67 files changed, 6964 insertions(+), 226 deletions(-)
local staged file remains: codex-test.txt
```

처음 `git pull`은 sandbox에서 `.git/FETCH_HEAD` 쓰기가 막혀 실패했고, 승인 권한으로 재실행해 성공했다.

### 2. 초기 배포 준비 점검

확인 명령:

```bash
node --version
npm --version
java -version
docker --version
```

초기 결과:

```text
Node: v22.22.2
npm: 10.9.7
Java: not found
Docker: not found
```

확인 사항:

```text
frontend/package.json requires Node >=24 <25
frontend/Dockerfile uses node:24-alpine
backend/Dockerfile uses eclipse-temurin:21
daily-outfit-weather/.env does not exist
```

### 3. 프론트 의존성, lint, build

명령:

```bash
cd daily-outfit-weather/frontend
npm ci
npm run lint
npm run build
npm audit --omit=dev
npm audit
```

결과:

```text
npm ci: success with EBADENGINE warning because local Node is v22.22.2
npm run lint: pass
npm run build: pass
npm audit --omit=dev: found 0 vulnerabilities
npm audit: 5 dev dependency vulnerabilities, 1 low and 4 high
```

### 4. PWA 설정 복구

수정 파일:

```text
frontend/vite.config.ts
```

수정 내용:

```text
VitePWA import and plugin activation
manifest.webmanifest generation
service worker generation
registerSW.js generation
navigateFallbackDenylist for /api/, /oauth2/, /login/
manifest icon configuration with favicon.svg and mascot/mild.png
```

검증 명령:

```bash
npm run lint
npm run build
find frontend/dist -maxdepth 2 -type f -print
curl -fsS -I http://127.0.0.1:4173/
curl -fsS http://127.0.0.1:4173/manifest.webmanifest
curl -fsS -I http://127.0.0.1:4173/sw.js
```

검증 결과:

```text
npm run lint: pass
npm run build: pass
dist/registerSW.js generated
dist/manifest.webmanifest generated
dist/sw.js generated
dist/workbox-9c191d2f.js generated
manifest HTTP response: 200
sw.js HTTP response: 200
```

### 5. 프론트 브라우저 알림 구현

서브에이전트 위임:

```text
Agent A: frontend/PWA notification worker
```

수정 파일:

```text
frontend/src/App.tsx
frontend/src/App.css
frontend/vite.config.ts
frontend/public/notification-sw.js
```

구현 내용:

```text
오늘 추천 화면에 알림 권한 요청 버튼 추가
권한 상태 표시: default, granted, denied, unsupported
추천 요약을 브라우저 알림으로 즉시 표시
ServiceWorkerRegistration.showNotification() 우선 사용
service worker 준비 전 fallback으로 new Notification() 사용
notification-sw.js에서 notificationclick 처리
알림 클릭 시 기존 PWA 창 focus 또는 앱 open
vite-plugin-pwa workbox importScripts에 /notification-sw.js 추가
```

검증:

```text
npm run lint: pass
npm run build: pass
dist/manifest.webmanifest generated
dist/registerSW.js generated
dist/sw.js generated
dist/notification-sw.js generated
dist/sw.js imports /notification-sw.js
compose frontend serves /manifest.webmanifest: HTTP 200
compose frontend serves /sw.js: HTTP 200
```

### 6. 백엔드 알림 자동 추천 생성과 한 줄 요약 구현

서브에이전트 위임:

```text
Agent B: backend notification worker
```

수정 파일:

```text
backend/src/main/java/com/dailyoutfitweather/notification/service/NotificationLogService.java
backend/src/main/java/com/dailyoutfitweather/notification/service/NotificationRecommendationSummary.java
backend/src/main/java/com/dailyoutfitweather/recommendation/service/RecommendationService.java
backend/src/test/java/com/dailyoutfitweather/notification/NotificationLogServiceTest.java
backend/src/test/java/com/dailyoutfitweather/notification/service/NotificationRecommendationSummaryTest.java
```

구현 내용:

```text
알림 생성 시 오늘 추천이 없으면 RecommendationService로 추천 생성
기존 추천이 있으면 재사용
중복 알림 로그가 있으면 추천 생성 전 반환
알림 body를 날씨 + 옷차림 + 준비물 한 줄로 생성
RecommendationService에 getOrCreateRecommendation(user, targetDate) 추가
```

알림 body 예시:

```text
출근 체감 16도, 퇴근 체감 13도, 강수확률 30% - 긴팔 티셔츠, 가벼운 재킷, 작은 우산
```

### 7. Java 설치와 백엔드 테스트

설치/확인 명령:

```bash
sudo apt-get update
sudo apt-get install -y openjdk-21-jdk
java -version
```

결과:

```text
OpenJDK 21.0.11 installed
```

초기 `./gradlew test` 실패 원인:

```text
JAVA_HOME/java not found
Gradle cache write blocked by sandbox
Docker/Testcontainers unavailable
Docker API client version mismatch
```

Docker/Testcontainers 해결 과정:

```text
docker.io installed
docker-compose-v2 installed
Docker daemon running
Docker client/server stabilized at 20.10.12 / 20.10.12
Testcontainers required docker-java API override: -Dapi.version=1.44
```

성공 명령:

```bash
cd daily-outfit-weather/backend
DOCKER_HOST=unix:///var/run/docker.sock \
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock \
./gradlew test --no-daemon -Dapi.version=1.44
```

결과:

```text
BUILD SUCCESSFUL in 4m 35s
57 tests completed
15 TEST-*.xml result files
0 failures
0 errors
0 skipped
```

서브에이전트 별도 검증:

```text
GRADLE_USER_HOME=/home/sujin941220/Playground/daily-outfit-weather/.gradle ./gradlew test --rerun-tasks
BUILD SUCCESSFUL in 2m 8s
XML 15개 확인, failure/error 없음
```

### 8. 백엔드 bootJar

명령:

```bash
cd daily-outfit-weather/backend
DOCKER_HOST=unix:///var/run/docker.sock \
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock \
./gradlew bootJar --no-daemon -Dapi.version=1.44
```

결과:

```text
BUILD SUCCESSFUL in 1m 4s
```

### 9. production-like Docker compose smoke

임시 env 파일:

```text
/tmp/dow-smoke.env
```

임시 값:

```dotenv
POSTGRES_DB=daily_outfit_weather
POSTGRES_USER=daily_outfit_weather
POSTGRES_PASSWORD=daily_outfit_weather
SPRING_PROFILES_ACTIVE=local
FRONTEND_PORT=18080
APP_FRONTEND_SUCCESS_URL=http://localhost:18080
APP_SECURITY_ALLOWED_ORIGINS=http://localhost:18080
APP_NOTIFICATION_GENERATE_DUE_TOKEN=local-smoke-token
APP_WEATHER_FALLBACK_ENABLED=true
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=lax
GOOGLE_CLIENT_ID=local-smoke-google-client-id
GOOGLE_CLIENT_SECRET=local-smoke-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:18080/login/oauth2/code/google
KMA_SERVICE_KEY=local-smoke-kma-key
KMA_BASE_URL=http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0
KMA_CONNECT_TIMEOUT=2s
KMA_READ_TIMEOUT=3s
```

기동 명령:

```bash
cd daily-outfit-weather
docker compose -p dow-agent-smoke --env-file /tmp/dow-smoke.env -f docker-compose.prod.yml up -d --build --force-recreate
```

결과:

```text
backend image built
frontend image built
postgres container healthy
backend container started
frontend container started on localhost:18080
```

초기 smoke:

```text
/api/health returned 502 while backend was still starting
backend completed startup after about 45.943 seconds
```

최종 smoke 명령:

```bash
BASE_URL=http://localhost:18080 scripts/deployment-smoke.sh
curl -fsS -I http://localhost:18080/manifest.webmanifest
curl -fsS -I http://localhost:18080/sw.js
```

결과:

```text
Checking health at http://localhost:18080/api/health
Checking OAuth redirect at http://localhost:18080/oauth2/authorization/google
Smoke checks passed

/manifest.webmanifest: HTTP 200
/sw.js: HTTP 200
```

정리 명령:

```bash
docker compose -p dow-agent-smoke --env-file /tmp/dow-smoke.env -f docker-compose.prod.yml down -v
```

결과:

```text
frontend/backend/postgres containers removed
backend-logs and postgres-data volumes removed
network removed
```

### 10. 최종 코드 리뷰

서브에이전트 위임:

```text
Agent E: final code review and test evidence reviewer
Agent F: deployment checklist explorer
Agent G: automatic notification scope reviewer
```

결과:

```text
blocking issue: none
backend JUnit XML: 57 tests, 0 failures, 0 errors, 0 skipped
frontend dist includes sw.js and notification-sw.js
production-like compose smoke passed
```

비차단 개선 후보:

```text
RecommendationService targetDate와 WeatherSnapshotProvider today 날짜 불일치 가능성
동시 알림 생성 시 추천 unique key 충돌 가능성
외부 날씨 조회 실패가 전체 due-log 작업을 롤백할 가능성
notification click URL exact match로 쿼리/해시가 다른 창 재사용이 안 될 수 있음
```

### 11. 최종 작업 트리 상태

명령:

```bash
git status --short --branch
```

결과:

```text
## main...origin/main
A  codex-test.txt
 M daily-outfit-weather/backend/src/main/java/com/dailyoutfitweather/notification/service/NotificationLogService.java
 M daily-outfit-weather/backend/src/main/java/com/dailyoutfitweather/recommendation/service/RecommendationService.java
 M daily-outfit-weather/backend/src/test/java/com/dailyoutfitweather/notification/NotificationLogServiceTest.java
 M daily-outfit-weather/frontend/src/App.css
 M daily-outfit-weather/frontend/src/App.tsx
 M daily-outfit-weather/frontend/vite.config.ts
?? daily-outfit-weather/backend/src/main/java/com/dailyoutfitweather/notification/service/NotificationRecommendationSummary.java
?? daily-outfit-weather/backend/src/test/java/com/dailyoutfitweather/notification/service/
?? daily-outfit-weather/frontend/public/notification-sw.js
```

`codex-test.txt`는 기존 staged 파일이므로 배포 커밋에 포함할지 별도 결정이 필요하다.

## 3. 현재 결론

코드와 로컬 production-like smoke 기준으로 배포 차단 이슈는 없다.

다만 실제 운영 배포는 아래가 준비되어야 완료할 수 있다.

1. 운영 `.env` secret
2. Google Console redirect URI 등록
3. 운영 서버 접속 권한
4. 운영 도메인 또는 GCP VM public IP 결정
5. 실제 브라우저 OAuth full-flow 확인
