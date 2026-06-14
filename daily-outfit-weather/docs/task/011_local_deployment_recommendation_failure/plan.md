# Plan: 011 Local Deployment Recommendation Failure

## Phase 1: Triage

- Inspect frontend request/error handling for the recommendation button flow.
- Inspect recommendation service and weather fallback behavior.
- Inspect deployment compose and production validator settings.
- Check running Docker container state and backend logs when permissions allow.

## Phase 2: Fix Direction

- Separate local smoke configuration from real production validation.
- Make weather fallback a deploy-time environment choice, defaulting to resilient behavior for local smoke.
- Avoid changing the recommendation engine or profile/location domain logic unless tests show a functional defect.

## Phase 3: Verification

- Run targeted recommendation/weather fallback tests.
- Re-run container smoke after configuration is corrected.
- Re-test login -> profile/location setup -> recommendation button in the browser after rebuilding the frontend/backend images.

## Follow-Up Candidates

- Add a dedicated local deployment compose file or documented command for production-like local smoke.
- Extend `scripts/deployment-smoke.sh` to fail fast when the backend container is restarting.
- Add an authenticated recommendation smoke helper that can run with a seeded test user outside OAuth.
