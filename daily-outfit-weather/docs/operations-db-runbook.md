# Operations DB Runbook

운영 PostgreSQL 백업, 복구, Flyway migration 적용, migration rollback 기준을 정리한다. 현재 운영형 Compose 기준 서비스명은 `postgres`, backend 서비스명은 `backend`, DB 볼륨은 `postgres-data`이다.

## 운영 원칙

- 운영 DB 변경 전에는 최신 백업 파일과 restore 검증 결과를 확보한다.
- Flyway migration 파일은 수정하지 않고 새 버전 파일로만 변경한다.
- 운영 장애 중에는 원인 미확인 상태에서 `flyway repair`, 수동 DDL, 데이터 삭제를 먼저 실행하지 않는다.
- 복구 작업은 담당자 1명이 명령을 실행하고 다른 1명이 대상 환경, 파일명, DB명을 교차 확인한다.
- 백업 파일은 DB 비밀번호, OAuth 사용자 이메일, 알림/피드백 데이터를 포함할 수 있으므로 외부 공유 저장소에 평문으로 두지 않는다.

## 환경 변수

운영 작업 shell에서 다음 값이 준비되어 있어야 한다.

```bash
export COMPOSE_FILE=docker-compose.prod.yml
export ENV_FILE=.env
export POSTGRES_DB=daily_outfit_weather
export POSTGRES_USER=daily_outfit_weather
```

백업 산출물은 기본적으로 로컬 `backups/` 디렉터리에 둔다. 실제 운영에서는 암호화된 object storage 또는 플랫폼 백업 저장소에 업로드한다.

```bash
mkdir -p backups
```

## 백업 실행

배포 전, migration 전, 장애 대응 전 백업을 만든다.

로컬/Compose 기본 컨테이너명 기준 빠른 실행:

```bash
set -a
source .env
set +a
COMPOSE_FILE=docker-compose.prod.yml ENV_FILE=.env scripts/db-backup.sh
```

기존 로컬 개발 compose의 고정 컨테이너명 기준으로 실행할 때는 `COMPOSE_FILE` 없이 실행한다.

```bash
set -a
source .env
set +a
scripts/db-backup.sh
```

세부 제어가 필요하면 아래 명령을 직접 실행한다.

```bash
BACKUP_FILE="backups/daily_outfit_weather_$(date -u +%Y%m%dT%H%M%SZ).dump"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  --format=custom --no-owner --no-privileges > "$BACKUP_FILE"
```

백업 직후 파일 크기와 custom dump catalog를 확인한다.

```bash
ls -lh "$BACKUP_FILE"
pg_restore --list "$BACKUP_FILE" | head
```

운영 서버에 `pg_restore`가 없으면 PostgreSQL client가 설치된 작업 머신에서 확인한다.

## Restore Drill

운영 DB에 직접 restore 연습을 하지 않는다. 임시 PostgreSQL 컨테이너에 restore해서 백업 유효성을 검증한다.

```bash
docker run --rm --name dow-restore-drill \
  -e POSTGRES_DB=restore_drill \
  -e POSTGRES_USER=restore_drill \
  -e POSTGRES_PASSWORD=restore_drill \
  -p 55432:5432 \
  -d postgres:16-alpine

until docker exec dow-restore-drill pg_isready -U restore_drill -d restore_drill; do sleep 1; done

pg_restore \
  --host localhost \
  --port 55432 \
  --username restore_drill \
  --dbname restore_drill \
  --clean --if-exists --no-owner --no-privileges \
  "$BACKUP_FILE"
```

검증 쿼리:

```bash
psql "postgresql://restore_drill:restore_drill@localhost:55432/restore_drill" -c '\dt'
psql "postgresql://restore_drill:restore_drill@localhost:55432/restore_drill" -c 'select count(*) from flyway_schema_history;'
psql "postgresql://restore_drill:restore_drill@localhost:55432/restore_drill" -c 'select count(*) from users;'
```

정리:

```bash
docker stop dow-restore-drill
```

## 운영 Restore

운영 restore는 데이터 손실 가능성이 있으므로 서비스 중단 창에서만 실행한다.

1. restore 대상 백업 파일명과 생성 시각을 확인한다.
2. backend를 중지해 DB 쓰기를 막는다.
3. 현재 운영 DB 백업을 한 번 더 만든다.
4. restore를 실행한다.
5. backend를 다시 시작하고 health check와 핵심 API를 확인한다.

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" stop backend

PRE_RESTORE_BACKUP="backups/pre_restore_$(date -u +%Y%m%dT%H%M%SZ).dump"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  --format=custom --no-owner --no-privileges > "$PRE_RESTORE_BACKUP"

cat "$BACKUP_FILE" | docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  --clean --if-exists --no-owner --no-privileges

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d backend
curl -i "http://localhost:${FRONTEND_PORT:-8080}/api/health"
```

로컬/Compose 기본 컨테이너명 기준 restore 스크립트:

```bash
set -a
source .env
set +a
COMPOSE_FILE=docker-compose.prod.yml ENV_FILE=.env scripts/db-restore.sh backups/<backup-file>.dump
```

기존 로컬 개발 compose의 고정 컨테이너명 기준으로 실행할 때는 `COMPOSE_FILE` 없이 실행한다.

```bash
set -a
source .env
set +a
scripts/db-restore.sh backups/<backup-file>.dump
```

`--clean`은 기존 객체를 삭제한 뒤 복구한다. 데이터 보존이 필요한 부분 복구에는 사용하지 않는다.

## Migration 배포 절차

현재 앱은 `spring.flyway.enabled=true`, `spring.jpa.hibernate.ddl-auto=validate` 기준이다. backend 시작 시 Flyway가 migration을 적용하고 JPA mapping을 검증한다.

배포 전:

```bash
cd backend
./gradlew test
```

운영 배포 직전:

```bash
BACKUP_FILE="backups/pre_migration_$(date -u +%Y%m%dT%H%M%SZ).dump"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  --format=custom --no-owner --no-privileges > "$BACKUP_FILE"
pg_restore --list "$BACKUP_FILE" | head
```

배포 후 migration 상태 확인:

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "select installed_rank, version, description, success, installed_on from flyway_schema_history order by installed_rank;"
```

서비스 확인:

```bash
curl -i "http://localhost:${FRONTEND_PORT:-8080}/api/health"
```

## Migration Rollback 기준

Flyway Community SQL migration은 자동 down migration을 제공하지 않는다. rollback은 상황별로 다음 중 하나를 선택한다.

### 앱 배포만 실패한 경우

DB migration이 성공했고 새 schema가 이전 앱과 호환되면 backend image만 이전 버전으로 되돌린다. 이 경우 DB restore는 하지 않는다.

확인할 항목:

- 새 migration이 nullable column 추가, index 추가처럼 이전 앱과 호환되는지
- 기존 column/table rename, drop, constraint 강화가 없는지
- 이전 앱 시작 시 `ddl-auto=validate`가 통과하는지

### Migration 적용 중 실패한 경우

1. backend를 중지한다.
2. `flyway_schema_history`에서 실패한 version과 description을 확인한다.
3. 실패 migration이 transaction 안에서 완전히 rollback되었는지 확인한다.
4. 객체가 일부 생성되었으면 수동 정리 SQL을 작성하기 전에 백업을 만든다.
5. 원인 수정 후 새 migration 파일 또는 같은 미적용 파일을 재배포한다.

```bash
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "select * from flyway_schema_history order by installed_rank desc limit 5;"
```

`flyway repair`는 실패 row 정리 용도로만 사용하고, schema 객체를 되돌리지 않는다는 점을 기록한다.

### 데이터 또는 schema 손상이 있는 경우

서비스를 중지하고 migration 직전 백업으로 운영 restore를 수행한다. restore 후에는 해당 migration을 포함한 backend를 다시 시작하지 않는다.

### 파괴적 schema 변경이 필요한 경우

운영 적용 전 별도 rollback plan을 migration PR 또는 작업 기록에 남긴다.

- 되돌릴 수 없는 데이터 변경 여부
- 이전 앱과의 호환 기간
- 백필 작업 중단/재시작 방법
- restore가 필요한 판단 기준
- restore 예상 소요 시간

## 정기 점검

- 매일: 자동 백업 성공 여부와 저장소 여유 공간 확인
- 매주: 최신 백업 1개 restore drill 실행
- 매월: 운영 restore 절차와 담당자 연락망 확인
- 배포 전: migration 직전 백업과 `./gradlew test` 결과 확인
