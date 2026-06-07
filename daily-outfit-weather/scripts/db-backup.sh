#!/usr/bin/env sh
set -eu

BACKUP_DIR="${BACKUP_DIR:-backups}"
TIMESTAMP="$(date +%Y%m%d%H%M%S)"
CONTAINER="${POSTGRES_CONTAINER:-daily-outfit-weather-postgres}"
COMPOSE_FILE="${COMPOSE_FILE:-}"
ENV_FILE="${ENV_FILE:-.env}"
DATABASE="${POSTGRES_DB:?POSTGRES_DB is required}"
USER="${POSTGRES_USER:?POSTGRES_USER is required}"

mkdir -p "$BACKUP_DIR"
OUTPUT="${BACKUP_DIR}/${DATABASE}-${TIMESTAMP}.dump"

if [ -n "$COMPOSE_FILE" ]; then
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
    pg_dump -U "$USER" -d "$DATABASE" -Fc > "$OUTPUT"
else
  docker exec "$CONTAINER" pg_dump -U "$USER" -d "$DATABASE" -Fc > "$OUTPUT"
fi

echo "Backup written to ${OUTPUT}"
