# GCP Temporary Deployment Runbook - 2026-07-29

## Current VM

```text
Public IP: 34.22.67.160
Current screen-check URL: http://34.22.67.160:8080
```

## Secret File Backups

The real `.env` files include secrets and must not be committed.

```text
backups/env-gcp-public-80-original-2026-07-29.env
backups/env-gcp-public-8080-2026-07-29.env
```

Both backup files are under `backups/`, which is ignored by Git.

## Mode 1: Public IP Port 8080 Screen Check

Use this mode to verify that the app builds, starts, and renders from the GCP VM.

`.env` shape:

```dotenv
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=8080
APP_FRONTEND_SUCCESS_URL=http://34.22.67.160:8080
APP_SECURITY_ALLOWED_ORIGINS=http://34.22.67.160:8080
GOOGLE_REDIRECT_URI=http://34.22.67.160:8080/login/oauth2/code/google
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=lax
```

Deploy:

```bash
DOCKER_API_VERSION=1.41 docker compose --env-file .env -f docker-compose.prod.yml up -d --build
```

Verify on the VM:

```bash
curl -i http://localhost:8080/api/health
curl -I http://localhost:8080/
```

Expected:

```text
http://localhost:8080/api/health -> 200 {"status":"ok"}
http://localhost:8080/ -> 200
```

Browser URL:

```text
http://34.22.67.160:8080
```

If this URL does not open from a laptop browser, check the GCP firewall and allow TCP `8080` ingress.

OAuth note:

```text
Google login may fail in this mode because raw public IP + HTTP is not a reliable Google OAuth Web redirect URL.
Use this mode mainly for screen and server checks.
```

## Mode 2: Original Public IP Port 80

This was the initial temporary public-IP shape.

`.env` shape:

```dotenv
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=80
APP_FRONTEND_SUCCESS_URL=http://34.22.67.160
APP_SECURITY_ALLOWED_ORIGINS=http://34.22.67.160
GOOGLE_REDIRECT_URI=http://34.22.67.160/login/oauth2/code/google
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=lax
```

Current blocker:

```text
Host port 80 is already used by system nginx.
```

Do not use this mode until port 80 is intentionally handled by either:

```text
1. stopping/reconfiguring system nginx, or
2. using system nginx as a reverse proxy to Docker frontend on 8080.
```

## Mode 3: Real Temporary Deployment With Login

Use this mode when the app should be usable by family with Google login.

Required:

```text
1. domain or temporary DNS name pointing to 34.22.67.160
2. HTTPS certificate
3. Google OAuth client updated to HTTPS origin and redirect URI
```

`.env` shape:

```dotenv
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=8080
APP_FRONTEND_SUCCESS_URL=https://<domain>
APP_SECURITY_ALLOWED_ORIGINS=https://<domain>
GOOGLE_REDIRECT_URI=https://<domain>/login/oauth2/code/google
SESSION_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=lax
```

Google Cloud Console:

```text
Authorized JavaScript origins:
https://<domain>

Authorized redirect URIs:
https://<domain>/login/oauth2/code/google
```

Recommended proxy shape:

```text
system nginx :80/:443 -> Docker frontend :8080 -> Docker backend :8080
```

## Current Result

As of 2026-07-29:

```text
Docker services are running.
frontend is mapped as 0.0.0.0:8080->80.
backend is using prod profile.
localhost health check passes.
```

Recommended immediate public URL:

```text
http://34.22.67.160:8080
```

If that public URL does not open outside the VM, open TCP `8080` in the GCP firewall.

## Current Result Update: System Nginx Port 80 Proxy

Public TCP `8080` timed out, while the app was healthy inside the VM. To make the app visible immediately without changing GCP firewall, system nginx on public port `80` was configured to proxy to Docker frontend on `127.0.0.1:8080`.

Nginx config prepared in repo:

```text
deploy/nginx-default-dow-proxy.conf
```

System nginx backup:

```text
/etc/nginx/sites-available/default.before-daily-outfit-weather-20260729
```

Applied config:

```text
/etc/nginx/sites-available/default
```

Validation:

```text
sudo nginx -t -> successful
sudo systemctl reload nginx -> successful
```

Current public URL:

```text
http://34.22.67.160
```

Checks:

```text
GET http://34.22.67.160/ -> 200
GET http://34.22.67.160/api/health -> 200 {"status":"ok"}
GET http://34.22.67.160/oauth2/authorization/google -> 302 to Google
redirect_uri=http://34.22.67.160/login/oauth2/code/google
```

OAuth caveat:

```text
Google Cloud Console is currently configured for localhost:8080.
Google login completion may fail for http://34.22.67.160 until the OAuth client is updated, and raw IP + HTTP may still be rejected by Google.
The real family-use deployment should use a domain + HTTPS.
```

## 2026-07-29 OAuth Approval Error

Observed from browser after opening the app and starting Google login:

```text
액세스 차단됨: 승인 오류
```

Current app OAuth request:

```text
GET http://34.22.67.160/oauth2/authorization/google -> 302 to Google
redirect_uri=http://34.22.67.160/login/oauth2/code/google
```

Known Google Console setting from the user:

```text
Authorized JavaScript origins:
http://localhost:8080

Authorized redirect URIs:
http://localhost:8080/login/oauth2/code/google
```

Likely mismatch:

```text
The app is now public at http://34.22.67.160, but Google OAuth is still configured for localhost:8080.
```

Next checks:

1. Try adding these values to the Google OAuth Web client:

```text
Authorized JavaScript origins:
http://34.22.67.160

Authorized redirect URIs:
http://34.22.67.160/login/oauth2/code/google
```

2. If Google refuses raw IP or HTTP values, move to domain + HTTPS.
3. If OAuth consent screen is Testing, add the target Google account to Test users.
4. Capture the exact Google error code/message if it appears. Useful codes include:

```text
redirect_uri_mismatch
origin_mismatch
access_denied
app_not_configured_for_user
```

Recommendation:

```text
Do not spend too much time trying to make raw IP + HTTP OAuth work.
The durable path is domain + HTTPS, then Google OAuth settings for that HTTPS origin.
```
