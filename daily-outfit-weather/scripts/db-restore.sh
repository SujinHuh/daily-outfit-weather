#!/usr/bin/env sh
set -eu

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <backup.dump>" >&2
  exit 1
fi

BACKUP_FILE="$1"
CONTAINER="${POSTGRES_CONTAINER:-daily-outfit-weather-postgres}"
COMPOSE_FILE="${COMPOSE_FILE:-}"
ENV_FILE="${ENV_FILE:-.env}"
DATABASE="${POSTGRES_DB:?POSTGRES_DB is required}"
USER="${POSTGRES_USER:?POSTGRES_USER is required}"

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Backup file not found: ${BACKUP_FILE}" >&2
  exit 1
fi

if [ -n "$COMPOSE_FILE" ]; then
  cat "$BACKUP_FILE" | docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
    pg_restore -U "$USER" -d "$DATABASE" --clean --if-exists
else
  cat "$BACKUP_FILE" | docker exec -i "$CONTAINER" pg_restore -U "$USER" -d "$DATABASE" --clean --if-exists
fi

echo "Restore completed from ${BACKUP_FILE}"
