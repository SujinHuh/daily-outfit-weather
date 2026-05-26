# API Spec

MVP API 명세 초안입니다. 실제 구현 시 validation error 등 세부 에러 응답은 보강합니다.

## 공통 전제

- Base path: `/api`
- 인증 방식은 Session Cookie 기반 OAuth로 시작합니다.
- Phase 1~4에서는 개발 편의를 위해 dev-only dummy user context를 사용할 수 있습니다.
- Phase 7 인증 연동 이후에는 모든 사용자 데이터 API가 인증된 사용자 본인 데이터만 조회/수정할 수 있어야 합니다.

## Auth

### Google OAuth 로그인

```http
GET /oauth2/authorization/google
```

### 내 정보 조회

```http
GET /api/me
```

Response:

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "수진"
}
```

## Profile

### 온보딩 저장

```http
POST /api/profile/onboarding
```

Request:

```json
{
  "nickname": "수진",
  "coldSensitivity": 4,
  "heatSensitivity": 2,
  "commuteTime": "08:30",
  "leaveWorkTime": "18:30",
  "notificationTime": "07:30",
  "transportType": "PUBLIC_TRANSPORT",
  "messageTone": "CHARACTER",
  "changeAlertOption": "IMPORTANT_ONLY",
  "homeLocation": {
    "sido": "서울특별시",
    "sigungu": "강남구",
    "dong": "역삼동"
  },
  "workLocation": {
    "sido": "서울특별시",
    "sigungu": "성동구",
    "dong": "성수동"
  }
}
```

### 프로필 조회

```http
GET /api/profile
```

### 프로필 수정

```http
PUT /api/profile
```

## Location

### 동 검색

```http
GET /api/locations/search?keyword=역삼
```

Response:

```json
[
  {
    "sido": "서울특별시",
    "sigungu": "강남구",
    "dong": "역삼동",
    "nx": 61,
    "ny": 125
  }
]
```

주의:

- 1차 구현에서는 동 단위 수동 입력 또는 정적 데이터 검색으로 시작할 수 있습니다.
- 지도 기반 위치 선택과 현재 위치 자동 감지는 MVP 제외 범위입니다.

## Weather

### 오늘 날씨 조회

```http
GET /api/weather/today
```

Response:

```json
{
  "current": {
    "temperature": 15,
    "feelsLikeTemperature": 12,
    "rainProbability": 20,
    "windSpeed": 4.2
  },
  "commute": {
    "time": "08:30",
    "temperature": 14,
    "feelsLikeTemperature": 11,
    "rainProbability": 20
  },
  "leaveWork": {
    "time": "18:30",
    "temperature": 10,
    "feelsLikeTemperature": 8,
    "rainProbability": 70
  }
}
```

주의:

- `feelsLikeTemperature`는 서비스 내부 계산값입니다.
- 미세먼지와 자외선은 1차 API 응답에서 제외합니다.

## Recommendation

### 오늘 추천 조회

```http
GET /api/recommendations/today
```

Response:

```json
{
  "id": 1,
  "targetDate": "2026-05-11",
  "summaryMessage": "오늘은 바람 때문에 생각보다 쌀쌀해요!",
  "characterImageType": "WINDY_LIGHT_OUTER",
  "topRecommendation": "얇은 니트",
  "outerRecommendation": "바람막이",
  "itemRecommendation": "작은 우산",
  "reason": "출근길은 괜찮지만 퇴근길에 비 예보가 있고, 바람이 강해 체감온도가 낮아요.",
  "weatherSummary": {
    "commuteFeelsLike": 11,
    "leaveWorkFeelsLike": 8,
    "rainProbability": 70,
    "windSpeed": 4.2
  }
}
```

### 오늘 추천 생성

```http
POST /api/recommendations/today
```

동작:

- 오늘 추천이 이미 있으면 기존 추천을 반환합니다.
- 오늘 추천이 없으면 사용자 프로필과 날씨 데이터를 기준으로 추천을 생성하고 저장합니다.

## Feedback

### 피드백 등록

```http
POST /api/recommendations/{recommendationId}/feedback
```

Request:

```json
{
  "temperatureFeedback": "COLD",
  "rainFeedback": "NEEDED",
  "comment": "퇴근길에 생각보다 추웠어요."
}
```

Response:

```json
{
  "id": 1,
  "recommendationId": 10,
  "temperatureFeedback": "COLD",
  "rainFeedback": "NEEDED",
  "comment": "퇴근길에 생각보다 추웠어요."
}
```

주의:

- `rainFeedback`은 `NEEDED`, `NOT_NEEDED` 중 하나를 사용합니다.
- 같은 추천에 다시 피드백을 등록하면 기존 피드백을 갱신합니다.
- 다른 사용자의 추천에는 피드백을 등록할 수 없습니다.

## Notification

### 알림 로그 조회

```http
GET /api/notifications
```

Response:

```json
[
  {
    "id": 1,
    "recommendationId": 10,
    "notificationType": "MORNING_REGULAR",
    "title": "오늘 뭐입지?",
    "body": "오늘은 가볍게 입어요.",
    "scheduledAt": "2026-05-25T22:30:00Z",
    "sentAt": null,
    "status": "PENDING",
    "failureReason": null,
    "createdAt": "2026-05-25T21:00:00Z"
  }
]
```

### 오늘 알림 로그 조회

```http
GET /api/notifications/today
```

### 알림 로그 생성(MVP)

```http
POST /api/notifications/generate-due
X-Internal-Job-Token: {internal-job-token}
```

Response:

```json
{
  "generatedCount": 3
}
```

동작:

- 현재 시각 기준으로 알림 시간이 지난 사용자 중 알림 옵션이 `OFF`가 아닌 사용자의 아침 알림 로그를 생성합니다.
- 실제 푸시 발송은 하지 않고 `PENDING` 상태의 로그만 저장합니다.
- 같은 사용자/알림 타입/예약 시각 조합은 중복 생성하지 않습니다.
- 내부 작업용 토큰이 없거나 일치하지 않으면 `403 Forbidden`을 반환합니다.

## 에러 응답 형식

기본 에러 응답은 `code`와 `message` 필드를 사용합니다.

```json
{
  "code": "PROFILE_NOT_FOUND",
  "message": "프로필을 찾을 수 없습니다."
}
```

## 추가 보강 필요

- validation error 응답 형식을 확정해야 합니다.
- 인증 실패/권한 실패 응답 코드를 확정해야 합니다.
