# GCP Temporary Deployment Runbook

Last updated: 2026-07-29

## Purpose

This note preserves the deployment variants used during the GCP temporary deployment work.
Do not paste real secret values into this document. Real `.env` snapshots are stored under
`backups/`, which is ignored by Git.

## Current VM

```text
GCP public IP: 34.22.67.160
```

## Preserved Env Snapshots

Secret-bearing snapshots:

```text
backups/.env.gcp-public-8080-2026-07-29
backups/.env.localhost-8080-2026-07-29
```

These files should stay local to the VM/workspace. They must not be committed.

## Variant 1: GCP Public Screen Check

Use this when the goal is to see the app running from the GCP VM in an external browser.

Expected URL:

```text
http://34.22.67.160:8080
```

Important limitation:

```text
Google login may fail on this URL because Google OAuth web clients generally require
HTTPS and do not reliably allow raw public IP HTTP redirect URIs.
```

Non-secret `.env` values:

```dotenv
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=8080
APP_FRONTEND_SUCCESS_URL=http://34.22.67.160:8080
APP_SECURITY_ALLOWED_ORIGINS=http://34.22.67.160:8080
GOOGLE_REDIRECT_URI=http://34.22.67.160:8080/login/oauth2/code/google
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=lax
APP_WEATHER_FALLBACK_ENABLED=true
```

Deploy:

```bash
DOCKER_API_VERSION=1.41 docker compose --env-file .env -f docker-compose.prod.yml up -d --build --force-recreate
```

Local smoke from the VM:

```bash
curl -i http://localhost:8080/api/health
```

External browser check:

```text
http://34.22.67.160:8080
```

If the local smoke passes but the external browser cannot connect, check the GCP firewall
rule for TCP `8080`.

## Variant 2: Localhost OAuth Validation

Use this only when validating Google OAuth against the current Google Cloud Console settings:

```text
Authorized JavaScript origins: http://localhost:8080
Authorized redirect URIs: http://localhost:8080/login/oauth2/code/google
```

Non-secret `.env` values:

```dotenv
SPRING_PROFILES_ACTIVE=local
FRONTEND_PORT=8080
APP_FRONTEND_SUCCESS_URL=http://localhost:8080
APP_SECURITY_ALLOWED_ORIGINS=http://localhost:8080
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=lax
APP_WEATHER_FALLBACK_ENABLED=true
```

Browser access from a local machine requires an SSH tunnel:

```bash
ssh -L 8080:localhost:8080 <VM_SSH_TARGET>
```

Then open:

```text
http://localhost:8080
```

## Variant 3: Temporary Real Deployment With Login

Use this when the app must be usable by family members from their own browsers.

Requirements:

```text
1. Real or temporary DNS name pointing to 34.22.67.160
2. HTTPS certificate
3. Google OAuth Console updated to the HTTPS domain
4. Port 80 conflict resolved, or system nginx used as a reverse proxy
```

Non-secret `.env` values:

```dotenv
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=80
APP_FRONTEND_SUCCESS_URL=https://<temporary-domain>
APP_SECURITY_ALLOWED_ORIGINS=https://<temporary-domain>
GOOGLE_REDIRECT_URI=https://<temporary-domain>/login/oauth2/code/google
SESSION_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=lax
APP_WEATHER_FALLBACK_ENABLED=true
```

Google Cloud Console values:

```text
Authorized JavaScript origins:
https://<temporary-domain>

Authorized redirect URIs:
https://<temporary-domain>/login/oauth2/code/google
```

Full smoke after HTTPS is available:

```bash
BASE_URL=https://<temporary-domain> scripts/deployment-smoke.sh
```

Manual browser smoke:

```text
1. Open app URL
2. Google login
3. /api/me
4. Onboarding save
5. Today recommendation
6. Feedback save
7. Logout and protected API behavior
```
