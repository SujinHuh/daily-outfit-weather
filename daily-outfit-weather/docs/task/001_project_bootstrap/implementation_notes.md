# Implementation Notes: 001 Project Bootstrap

## 진행 로그

- **2026-05-12:** 구현 전 문서 정합성 검토 완료.
- **2026-05-12:** Pending decision 정리 완료.
- **2026-05-12:** Node.js 24 LTS, Testcontainers PostgreSQL, Docker Compose service/database naming 기준 확정.
- **2026-05-12:** Gemini가 생성한 full overlay 관련 변경은 DEC-010과 충돌하므로 되돌림. Full overlay 도입은 별도 결정 전까지 보류.
- **2026-05-12:** Backend/Frontend/Docker Compose bootstrap 산출물 정리 진행.
- **2026-05-12:** Backend `./gradlew test` 성공.
- **2026-05-12:** Frontend `npm run build` 성공. Node.js 24.15.0 기준으로 재검증 완료.
- **2026-05-12:** `docker compose config` 성공.
- **2026-05-12:** `docker compose up -d postgres`, `docker compose ps`, `pg_isready` 성공.
- **2026-05-12:** Backend `./gradlew bootRun` 후 `/api/health`가 HTTP 200과 `{"status":"ok"}`를 반환함.

## 구현 중 결정 사항

- **Spring Boot:** 3.x 계열을 사용한다. Phase 1 bootstrap 생성물은 Spring Boot 3.5.0 기준으로 정리한다.
- **Java:** OpenJDK 21 (Microsoft build 확인됨).
- **Node.js:** 24.x LTS 사용.
- **Database:** PostgreSQL 16-alpine 사용 (Docker Compose).
- **Persistence:** 초기 CRUD 중심 구현을 전제로 Spring Data JPA를 사용한다. MyBatis 도입은 복잡 조회 요구가 생길 때 별도 결정한다.

## 사용자 승인 필요 항목

- 없음

## 후속 태스크 후보

- Phase 2: 사용자/프로필/위치 기본 도메인 구현
- Phase 3: 기상청 API 연동 및 추천 엔진 구현
