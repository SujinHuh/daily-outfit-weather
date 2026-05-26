# Validation Report: 002 User Profile Location

## 상태

검증 완료.

## 검증 항목

- [x] 온보딩 저장 API 통합 테스트
- [x] 프로필 조회 API 통합 테스트
- [x] 프로필 수정 API 통합 테스트
- [x] validation 실패 테스트
- [x] 프로필 미생성 조회/수정 실패 테스트
- [x] 문자열 길이 validation 테스트
- [x] 사용자별 profile/location 중복 생성 방지 테스트
- [x] `./gradlew test`

## 실행한 검증

```bash
cd backend
./gradlew compileJava compileTestJava
```

결과: 성공.

```bash
cd backend
./gradlew test
```

결과: 성공. Testcontainers PostgreSQL 기반으로 Flyway migration, JPA mapping, MockMvc API 통합 테스트를 검증했다.

## 실행하지 못한 검증

없음.

## 남은 리스크

- 현재 사용자 컨텍스트는 Phase 7 전까지 dev-only dummy user다.
- `nx`, `ny`는 Phase 5 기상청 좌표 매핑 전까지 nullable로 유지한다.
- 동시 요청 경합에서 unique constraint 예외를 사용자 친화 에러로 매핑하는 처리는 Phase 7 인증/실사용 흐름에서 추가 보강이 필요하다.
