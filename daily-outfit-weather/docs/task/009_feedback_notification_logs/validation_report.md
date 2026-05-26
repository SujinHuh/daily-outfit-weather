# Validation Report: 009 Feedback and Notification Logs

## 상태

**Phase 8 구현 및 검증 완료**

## 실행한 검증

- `npm run lint` 성공
- `npm run build` 성공
- `./gradlew test` 성공

## 추가한 테스트

- 피드백 저장 성공
- 피드백 재등록 시 갱신
- 다른 사용자 추천 피드백 차단
- 빈 피드백 요청 거절
- 알림 대상 시간 매칭
- 알림 옵션 `OFF` 제외
- 같은 예약 알림 로그 중복 생성 방지

## 서브 에이전트 검수 반영

- API/DB 미노출 항목 구현
- Flyway V3 추가
- 추천 소유권 검증 경로 추가
- `rainFeedback` 명칭을 `NEEDED`/`NOT_NEEDED`로 통일
- NotificationLog를 `notificationType`, `title`, `body`, `scheduledAt`, `sentAt`, `status`, `failureReason` 구조로 보강
- Phase 8 테스트 추가

## 추가 검수 기록

- 2026-05-26 재검수에서 `POST /api/notifications/generate-due`가 로그인 사용자 누구나 전체 사용자 대상 알림 로그 생성을 트리거할 수 있는 점을 확인했다. 운영성 엔드포인트이므로 관리자/내부 잡/스케줄러 전용 접근 제어로 제한하거나 응답을 생성 개수 수준으로 축소해야 한다.
- 2026-05-26 재검수에서 알림 로그 중복 방지가 `exists` 확인 후 `save` 흐름이라 동시 호출 시 DB unique 제약 충돌로 500 응답이 발생할 수 있는 점을 확인했다. insert-on-conflict, 예외 흡수, 잠금 중 하나로 보완해야 한다.

## 재검증

- `./gradlew test` 성공
- `npm run lint` 성공
- `npm run build` 성공
