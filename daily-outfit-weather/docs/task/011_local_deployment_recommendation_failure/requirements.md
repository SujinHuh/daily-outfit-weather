# Requirements: 011 Local Deployment Recommendation Failure

## Goals

- Restore the local deployed stack so API requests are served by a running backend.
- Preserve strict production validation for real external deployment.
- Keep today's recommendation resilient when KMA configuration, network, or API response fails.
- Record the triage and validation in the Harness Kit task workspace.

## Functional Requirements

- Local deployment smoke must not run the strict production validator with localhost URLs unless explicitly intended.
- Real production deployment must still reject placeholder or localhost OAuth/frontend/security settings.
- Recommendation generation should continue with default weather snapshots when the KMA client cannot return data, matching `005 Weather API Integration`.

## Configuration Requirements

- `SPRING_PROFILES_ACTIVE` must be explicit for local smoke versus real production.
- `APP_WEATHER_FALLBACK_ENABLED` must be configurable from the environment instead of being hard-coded off in compose.

## Validation Requirements

- Run targeted backend tests for recommendation/weather fallback behavior.
- If Docker access is available, inspect container state and backend logs.
- Record unexecuted browser-level validation if real OAuth credentials or browser smoke cannot be completed in the current turn.
