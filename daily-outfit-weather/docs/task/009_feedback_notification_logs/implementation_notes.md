# Implementation Notes: 009 Feedback and Notification Logs

## 진행 로그

- **2026-05-25:** `recommendation_feedbacks`, `notification_logs` Flyway V3 마이그레이션 추가.
- **2026-05-25:** 피드백 도메인, DTO, Repository, Service, Controller 구현.
- **2026-05-25:** 추천 소유권 확인을 위해 `OutfitRecommendationRepository.findByIdAndUserId` 추가.
- **2026-05-25:** 알림 로그 도메인, 조회 API, due 로그 생성 로직 구현.
- **2026-05-25:** 프론트 피드백 UI를 저장 API에 연결.
- **2026-05-25:** 서브 에이전트 검수 반영: `NOT_NEEDED` enum 통일, NotificationLog 필드 보강, 테스트 추가.

## 구현 결정

- MVP 알림은 `MORNING_REGULAR` 타입만 생성한다.
- 실제 발송은 Phase 8 범위가 아니므로 생성된 로그는 `PENDING` 상태로 저장한다.
- 같은 사용자/알림 타입/예약 시각은 중복 생성하지 않는다.
- 피드백은 추천당 하나만 유지하며 재등록 시 갱신한다.
