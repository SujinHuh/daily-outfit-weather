# Operations Logs and Monitoring Runbook

운영 로그 확인, 보관, 모니터링, 알림 기준을 정리한다. 현재 운영형 Compose에서 backend 로그 파일은 `LOG_FILE` 환경변수로 제어하며 기본값은 `/app/logs/backend.log`, 볼륨은 `backend-logs`이다.

## 운영 원칙

- 장애 대응 중에는 먼저 증거를 수집하고, 재시작은 영향과 복구 기준을 확인한 뒤 실행한다.
- 로그에 OAuth 사용자 이메일, 위치, 피드백 코멘트가 포함될 수 있으므로 외부 공유 시 필요한 줄만 최소화한다.
- 운영 secret, OAuth client secret, KMA service key, internal job token은 로그나 이슈 본문에 남기지 않는다.
- 컨테이너 로그와 파일 로그를 모두 확인한다. 파일 로그는 보관용, 컨테이너 로그는 즉시 상태 확인용으로 사용한다.

## 빠른 상태 확인

```bash
export COMPOSE_FILE=docker-compose.prod.yml
export ENV_FILE=.env

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
curl -i "http://localhost:${FRONTEND_PORT:-8080}/api/health"
```

PostgreSQL readiness:

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"
```

최근 backend 로그:

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=200 backend
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T backend tail -200 /app/logs/backend.log
```

## 로그 위치

- backend 컨테이너 stdout/stderr: `docker compose logs backend`
- backend 파일 로그: `/app/logs/backend.log`
- backend 로그 볼륨: `backend-logs`
- PostgreSQL 컨테이너 로그: `docker compose logs postgres`
- frontend/nginx 컨테이너 로그: `docker compose logs frontend`

파일 로그를 호스트로 복사:

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" cp backend:/app/logs/backend.log ./backend.log
```

## 필수 모니터링 항목

### Availability

- `GET /api/health` 200 응답
- backend container running 상태
- frontend/nginx container running 상태
- postgres healthcheck healthy 상태

권장 알림:

- `/api/health` 2회 연속 실패
- backend 또는 postgres 컨테이너 재시작 발생
- frontend 5xx 응답률 증가

### Database

- PostgreSQL disk 사용량
- PostgreSQL connection 수
- long-running query
- `flyway_schema_history.success=false`
- 백업 작업 성공 여부와 백업 파일 생성 시각

점검 쿼리:

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "select count(*) as active_connections from pg_stat_activity;"

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "select pid, now() - query_start as age, state, left(query, 120) from pg_stat_activity where state <> 'idle' order by age desc limit 10;"

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "select version, description, success, installed_on from flyway_schema_history order by installed_rank desc limit 5;"
```

### External Dependencies

- Google OAuth redirect/login 성공 여부
- KMA API 오류율과 timeout
- `WEATHER_UNAVAILABLE` 응답 증가
- internal notification generation job 403/5xx 증가

로그 검색 예시:

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs backend | grep -E "WEATHER_UNAVAILABLE|WeatherApiException|OAuth|generate-due"
```

## 로그 보관과 회전

현재 Compose는 `backend-logs` 볼륨에 파일 로그를 남긴다. 운영 플랫폼에서는 다음 중 하나를 반드시 추가한다.

- Docker logging driver 또는 플랫폼 로그 수집기로 stdout/stderr 수집
- `/app/logs/backend.log`를 수집하는 agent 연결
- volume snapshot 또는 파일 로그 rotation 정책

권장 보관 기준:

- 애플리케이션 로그: 30일
- 보안/OAuth 관련 이벤트: 90일
- 백업/restore/migration 작업 로그: 1년

로그 파일이 디스크를 채우지 않도록 운영 host 또는 플랫폼에서 rotation을 설정한다. Compose 단독 운영이라면 Docker daemon의 `json-file` log rotation과 backend 파일 로그 rotation 정책을 별도로 둔다.

## Incident Triage

### Health Check 실패

1. frontend/nginx, backend, postgres 컨테이너 상태를 확인한다.
2. backend 최근 로그에서 startup failure, Flyway failure, DB connection failure를 확인한다.
3. postgres readiness와 connection 수를 확인한다.
4. 최근 배포 또는 migration이 있었다면 [Operations DB Runbook](./operations-db-runbook.md)의 migration rollback 기준을 따른다.

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=200 backend
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=100 postgres
```

### OAuth 로그인 실패

1. Google Console redirect URI와 `APP_FRONTEND_SUCCESS_URL`을 확인한다.
2. proxy가 `X-Forwarded-Proto: https`를 전달하는지 확인한다.
3. session cookie `Secure`, `SameSite` 설정이 배포 topology에 맞는지 확인한다.
4. CORS origin이 운영 frontend origin만 포함하는지 확인한다.

### KMA 날씨 API 장애

1. backend 로그에서 timeout, service key 오류, KMA response code를 확인한다.
2. 운영에서는 `APP_WEATHER_FALLBACK_ENABLED=false`이므로 장애 시 `WEATHER_UNAVAILABLE`이 정상적으로 반환될 수 있다.
3. 장애가 외부 API 원인이면 사용자 영향 범위와 재시도 시각을 기록한다.

### Notification Job 실패

1. `X-Internal-Job-Token`이 운영 secret과 일치하는지 확인한다.
2. `notification_logs`의 최근 status와 failure reason을 확인한다.
3. job 중복 실행 여부를 `uk_notification_logs_user_type_scheduled_at` unique constraint 기준으로 확인한다.

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "select status, count(*) from notification_logs where created_at > now() - interval '1 day' group by status;"
```

## 배포 후 Smoke Check

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
curl -i "http://localhost:${FRONTEND_PORT:-8080}/api/health"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=100 backend
```

기본 health와 OAuth redirect만 빠르게 확인하려면 다음 스크립트를 사용한다.

```bash
BASE_URL="http://localhost:${FRONTEND_PORT:-8080}" scripts/deployment-smoke.sh
```

브라우저 또는 API로 확인할 항목:

- Google OAuth login redirect
- `GET /api/me`
- 온보딩 저장
- 오늘 추천 생성
- 피드백 저장
- KMA 장애 또는 미지원 위치에서 `WEATHER_UNAVAILABLE` 처리

## 운영 기록

다음 작업은 날짜, 담당자, 명령 요약, 결과를 `docs/deployment-readiness-log.md` 또는 별도 운영 일지에 남긴다.

- 운영 배포
- DB backup과 restore drill
- migration 적용 또는 실패
- production incident
- OAuth, CORS, cookie 정책 변경
