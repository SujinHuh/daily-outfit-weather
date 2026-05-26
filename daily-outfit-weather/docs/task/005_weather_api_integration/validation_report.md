# Validation Report: 005 Weather API Integration

## 상태

부분 검증 완료.

## 검증 항목

- [x] 기상청 카테고리 parser 단위 테스트
- [x] 비/눈/빗방울/눈날림 강수유형 매핑 테스트
- [x] 필수 카테고리 누락 오류 테스트
- [x] 발표시각 계산 테스트
- [x] 00:00~00:10 이전 날짜 base time 보정 테스트
- [x] target forecast time 기준 base time 선택 테스트
- [x] KMA client URL/serviceKey/resultCode 처리 테스트
- [x] 좌표 누락/API 실패 fallback 테스트
- [x] 한쪽 위치 실패 시 부분 fallback 테스트
- [x] null user/profile 방어 테스트
- [x] 추천 서비스 연동 테스트
- [x] `./gradlew compileJava compileTestJava`
- [x] `./gradlew test --tests 'com.dailyoutfitweather.weather.*' --tests com.dailyoutfitweather.recommendation.WeatherSnapshotProviderTest --tests com.dailyoutfitweather.recommendation.RecommendationServiceTest --tests com.dailyoutfitweather.recommendation.engine.RuleBasedRecommendationEngineTest`
- [x] `./gradlew test`
- [ ] 실제 KMA API 키 기반 live smoke test

## 실행한 검증

```bash
cd backend
./gradlew compileJava compileTestJava
./gradlew test --tests 'com.dailyoutfitweather.weather.*' --tests com.dailyoutfitweather.recommendation.WeatherSnapshotProviderTest --tests com.dailyoutfitweather.recommendation.RecommendationServiceTest --tests com.dailyoutfitweather.recommendation.engine.RuleBasedRecommendationEngineTest
```

결과: 성공.

## 추가 실행한 검증

```bash
cd backend
./gradlew test
```

결과: 성공. Docker Desktop 실행 후 Testcontainers PostgreSQL 통합 테스트까지 통과했다.

## 실행하지 못한 검증

실제 KMA API 호출은 로컬에 `.env`와 `KMA_SERVICE_KEY` 환경변수가 없어 실행하지 않았다.

## 서브 에이전트 검수 반영

- KMA timeout 부재: connect/read timeout 설정을 추가했다. 기본값은 `2s`/`3s`다.
- 최신 base time 고정 리스크: target forecast time을 포함할 수 있는 발표시각을 선택하도록 `baseTimeForTarget`을 추가했다.
- target time 내림 리스크: 30분대 출퇴근 시간은 다음 정시 forecast로 조회하도록 변경했다.
- service key 인코딩 불명확: 공공데이터포털 Encoding 인증키 사용을 `.env.example`에 명시하고 client URL 테스트를 추가했다.
- client-level 검증 부족: URL, encoded service key, `resultCode != 00` 처리 테스트를 추가했다.
- 좌표/API 실패 fallback: 좌표 누락과 API 실패 시 기본 스냅샷 반환 테스트를 추가했다.

## Gimin 심층 검수 반영

- 00:00~00:10 사이 base time 날짜 보정 오류를 수정했다. `minusMinutes(10)` 이후의 날짜와 시간을 함께 사용한다.
- PTY 5는 비, PTY 6/7은 눈으로 매핑하도록 확장했다.
- fallback 시 `warn` 로그를 남기도록 했다.
- HOME/WORK 중 한쪽 조회만 실패하면 실패한 쪽만 기본 스냅샷으로 대체하도록 부분 fallback으로 바꿨다.
- `user`, `profile` null 입력은 명시적으로 거부한다.
- service key는 encoded key 보존 테스트로 고정했다.

## Live smoke 준비 상태

- 로컬 `.env` 파일 없음.
- 현재 shell 환경에 `KMA_SERVICE_KEY` 없음.
- `KMA_SERVICE_KEY`가 준비되면 `KMA_SERVICE_KEY=<Encoding 인증키> ./gradlew test --tests 'com.dailyoutfitweather.weather.*'`와 추천 API 수동 호출로 확인한다.

## 남은 리스크

- 프로필 위치에 `nx`, `ny`가 없으면 정적 KMA grid catalog로 자동 보강한다. catalog에 없는 위치는 기본 스냅샷으로 폴백한다.
- 외부 API 장애 시 추천 자체는 반환되지만, 사용자에게 폴백 여부를 노출하는 응답 필드는 아직 없다.
- WeatherForecast 저장/캐시는 아직 구현하지 않았다.
