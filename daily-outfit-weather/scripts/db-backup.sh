#!/usr/bin/env sh
set -eu

BACKUP_DIR="${BACKUP_DIR:-backups}"
TIMESTAMP="$(date +%Y%m%d%H%M%S)"
CONTAINER="${POSTGRES_CONTAINER:-daily-outfit-weather-postgres}"
DATABASE="${POSTGRES_DB:?POSTGRES_DB is required}"
USER="${POSTGRES_USER:?POSTGRES_USER is required}"

mkdir -p "$BACKUP_DIR"
OUTPUT="${BACKUP_DIR}/${DATABASE}-${TIMESTAMP}.dump"

docker exec "$CONTAINER" pg_dump -U "$USER" -d "$DATABASE" -Fc > "$OUTPUT"

echo "Backup written to ${OUTPUT}"
