#!/usr/bin/env sh
set -eu

BASE_URL="${BASE_URL:-http://localhost:${FRONTEND_PORT:-8080}}"

echo "Checking health at ${BASE_URL}/api/health"
curl -fsS -i "${BASE_URL}/api/health" | grep -q '"status":"ok"'

echo "Checking OAuth redirect at ${BASE_URL}/oauth2/authorization/google"
status="$(curl -s -o /dev/null -w '%{http_code}' "${BASE_URL}/oauth2/authorization/google")"
if [ "$status" != "302" ]; then
  echo "Expected OAuth redirect HTTP 302, got ${status}" >&2
  exit 1
fi

echo "Smoke checks passed"
