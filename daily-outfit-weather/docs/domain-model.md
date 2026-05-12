# Domain Model

`오늘 뭐입지?` MVP의 주요 도메인 모델을 정리한 문서입니다.

## User

사용자 계정 정보입니다.

주요 속성:

- id
- email
- nickname
- provider
- providerId
- createdAt
- updatedAt

## UserProfile

추천 생성을 위한 사용자 설정 정보입니다.

주요 속성:

- userId
- coldSensitivity
- heatSensitivity
- commuteTime
- leaveWorkTime
- notificationTime
- transportType
- messageTone
- changeAlertOption

주의:

- `changeAlertOption`은 MVP 1차에서 설정값 저장 용도로만 사용합니다.
- 실제 날씨 변경 알림 로직은 후순위 기능입니다.

## Location

사용자의 집/직장 위치 정보입니다.

주요 속성:

- id
- userId
- type
- sido
- sigungu
- dong
- nx
- ny

위치 타입:

- HOME
- WORK

## WeatherForecast

날씨 조회 결과입니다.

주요 속성:

- id
- locationId
- forecastDate
- forecastTime
- temperature
- feelsLikeTemperature
- minTemperature
- maxTemperature
- rainProbability
- precipitationType
- windSpeed
- humidity
- rawData

주의:

- `feelsLikeTemperature`는 기상청 단기예보 API 직접 응답이 아니라 서비스 내부 계산값으로 다룹니다.
- `dustLevel`, `uvIndex`는 1차 구현 제외 또는 후순위 외부 API 연동 대상으로 둡니다.

## OutfitRecommendation

추천 결과입니다.

주요 속성:

- id
- userId
- targetDate
- summaryMessage
- topRecommendation
- outerRecommendation
- itemRecommendation
- characterImageType
- reason
- weatherSnapshot
- recommendationType
- createdAt

주의:

- `characterImageType`은 추천 당시 어떤 이미지 타입이 선택됐는지 기록하기 위해 저장합니다.

## Feedback

추천 피드백입니다.

주요 속성:

- id
- recommendationId
- userId
- temperatureFeedback
- rainFeedback
- comment
- createdAt

`temperatureFeedback` 예시:

- COLD
- GOOD
- HOT

`rainFeedback` 예시:

- NEEDED
- NOT_NEEDED

## NotificationLog

알림 로그입니다.

주요 속성:

- id
- userId
- recommendationId
- notificationType
- title
- body
- scheduledAt
- sentAt
- status
- failureReason
- createdAt

`notificationType` 예시:

- MORNING_REGULAR
- WEATHER_CHANGE

`status` 예시:

- PENDING
- SENT
- FAILED
- SKIPPED
