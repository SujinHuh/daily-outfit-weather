# 오늘 뭐입지? - Weather Fit (Original Brief)

> 원본 기획/설계 문서입니다. 구현 기준 레포명은 `daily-outfit-weather`, 서비스명은 `오늘 뭐입지?`, 영문 표기는 `Daily Outfit Weather`로 정리했습니다. 기존 `Weather Fit` / `weather-fit` 표기는 원본 아이디어 이름입니다. 실제 개발 기준은 `README.md`의 주요 문서 링크 아래 개별 문서들을 우선하며, 특히 `docs/product-requirements.md`, `docs/development-plan.md`, `docs/api-spec.md`, `docs/database-design.md`를 우선합니다.
>
> 미세먼지와 자외선은 원본 아이디어에는 포함되어 있지만, 기상청 단기예보 API에서 바로 제공되는 값이 아니므로 1차 구현에서는 제외하거나 후순위 연동 대상으로 둡니다.

> 출근 전 오늘의 날씨, 체감온도, 이동 동선, 개인 민감도를 반영해 옷차림과 준비물을 알려주는 개인화 아침 알림 서비스

## 1. 프로젝트 개요

**오늘 뭐입지?**는 사용자가 아침에 출근하거나 외출하기 전, 오늘 날씨에 맞는 옷차림과 준비물을 빠르게 결정할 수 있도록 도와주는 서비스이다.

기존 날씨 앱은 기온, 강수확률, 미세먼지 등 날씨 정보를 제공하지만, 사용자는 여전히 다음과 같은 고민을 직접 해야 한다.

- 오늘 겉옷을 챙겨야 할까?
- 우산을 가져가야 할까?
- 출근길은 괜찮은데 퇴근길에는 추워지지 않을까?
- 바람 때문에 실제보다 더 춥게 느껴지지 않을까?
- 나는 추위를 많이 타는데 일반적인 추천이 나에게 맞을까?

이 서비스는 단순히 날씨 정보를 보여주는 것이 아니라, 사용자의 출근 시간, 퇴근 시간, 집/직장 위치, 추위/더위 민감도, 이동 방식 등을 바탕으로 **오늘 입을 옷과 챙길 물건을 추천**한다.

## 2. 프로젝트 목표

### 2.1 1차 목표

개인 사용을 위한 MVP를 완성한다.

- 매일 아침 앱을 열어 오늘 옷차림을 확인할 수 있다.
- 사용자는 집/직장 위치와 알림 시간을 설정할 수 있다.
- 기상청 단기예보 API를 기반으로 오늘 날씨를 조회한다.
- 출근/퇴근 시간대 날씨를 기준으로 옷차림과 준비물을 추천한다.
- 사용자는 추천이 맞았는지 피드백을 남길 수 있다.

### 2.2 2차 목표

포트폴리오로 활용 가능한 구조를 만든다.

- 외부 API 연동
- 사용자별 설정 관리
- 룰 기반 추천 엔진
- 알림 스케줄링 구조
- 추천 결과 저장
- 피드백 기반 개인화 확장
- 위치/날씨 데이터 관리
- 알림 로그 관리
- 실서비스 확장 가능한 아키텍처 설계

### 2.3 3차 목표

실사용 후 배포 및 상용화 가능성을 검토한다.

- 본인 및 가족 테스트
- 지인 5~10명 대상 피드백 수집
- 반복 사용률 확인
- 추천 정확도 개선
- 웹 푸시/FCM 연동
- 캐릭터 커스터마이징
- 가족 프로필
- 컨디션/생리주기 반영
- 유료 기능 가능성 검토

## 3. 서비스 한 줄 정의

> 오늘 날씨를 알려주는 앱이 아니라, 오늘 내가 춥지 않게 나갈 수 있도록 옷차림을 결정해주는 앱

## 4. 핵심 사용자

### 4.1 1차 사용자

출근 전 매일 날씨와 옷차림을 고민하는 직장인

예시:

- 아침마다 일기예보를 확인하는 사람
- 겉옷을 챙길지 자주 고민하는 사람
- 우산을 자주 놓치는 사람
- 추위/더위 민감도가 일반적인 추천과 잘 맞지 않는 사람
- 출근길과 퇴근길 날씨 차이 때문에 옷차림을 고민하는 사람

### 4.2 2차 사용자

부모님 세대도 사용할 수 있는 쉬운 UX를 원하는 사용자

예시:

- 날씨 정보를 복잡하게 보기 어려운 사용자
- "오늘 외투 챙겨", "우산 챙겨"처럼 간단한 안내를 원하는 사용자
- 가족에게 오늘 옷차림을 알려주고 싶은 사용자

## 5. 핵심 가치

### 5.1 사용자의 행동을 줄인다

사용자는 더 이상 날씨 정보를 보고 직접 판단하지 않아도 된다.

기존 방식:

```text
날씨 앱 확인
→ 기온 확인
→ 체감온도 확인
→ 강수확률 확인
→ 퇴근 시간 날씨 확인
→ 옷차림 직접 판단
```

서비스 방식:

```text
앱 확인
→ 오늘 추천 확인
→ 옷차림 결정
```

### 5.2 출근/퇴근 시간을 함께 고려한다

아침 날씨만 보고 옷을 입으면 퇴근길에 춥거나 비를 맞을 수 있다.

따라서 이 서비스는 다음을 함께 고려한다.

- 현재 날씨
- 출근 시간대 날씨
- 퇴근 시간대 날씨
- 일교차
- 강수 시간대
- 바람
- 미세먼지

### 5.3 개인 민감도를 반영한다

같은 17도라도 사람마다 다르게 느낀다.

- 추위를 많이 타는 사람
- 더위를 많이 타는 사람
- 바람에 민감한 사람
- 실내 냉방에 약한 사람
- 대중교통/도보/자차 이동 방식 차이

MVP에서는 간단한 설정값으로 반영하고, 이후 피드백 데이터를 기반으로 개인화 보정을 확장한다.

## 6. 기술 스택

### 6.1 Frontend

- React
- Vite
- TypeScript
- PWA

선택 이유:

- 빠른 개발 환경
- 단순한 SPA 구조
- 백엔드 API와 역할 분리 명확
- 모바일 홈 화면 설치 가능
- 앱처럼 사용할 수 있는 경험 제공
- 추후 웹 푸시 확장 가능

### 6.2 Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA

선택 이유:

- Spring Boot 기반 백엔드 구조 학습 및 포트폴리오 활용 가능
- REST API 구현에 적합
- 스케줄링, 인증, DB 연동, 외부 API 연동 구조를 보여주기 좋음
- Codex 기반 코드 생성 및 검토가 상대적으로 안정적

Kotlin 도입 계획:

초기 MVP에서는 Java로 구현한다. 이후 2차 단계에서 다음 모듈 일부를 Kotlin으로 도입할 수 있다.

- RecommendationRuleEngine
- WeatherApiClient
- NotificationMessageGenerator

### 6.3 Database

- PostgreSQL

PostgreSQL은 JSONB 타입을 활용해 다음 데이터를 유연하게 저장하기 좋다.

- 기상청 API 응답 스냅샷
- 추천 생성 당시의 날씨 조건
- 추천 판단 근거
- 알림 발송 당시 데이터
- 추후 분석용 데이터

예시:

```json
{
  "temperature": 12,
  "feelsLike": 9,
  "rainProbability": 70,
  "windSpeed": 5.2,
  "dustLevel": "BAD",
  "forecastTime": "18:00"
}
```

### 6.4 Authentication

- Google OAuth

선택 이유:

- 이메일/비밀번호 기반 회원가입 기능을 직접 만들 필요가 없음
- 사용자 진입장벽이 낮음
- OAuth 인증 경험을 포트폴리오에서 보여줄 수 있음
- MVP에서 빠르게 로그인 기능 구현 가능

### 6.5 Weather API

- 기상청 단기예보 API

선택 이유:

- 한국 사용자를 대상으로 하는 서비스에 적합
- 출근/퇴근 시간대 날씨 예보 활용 가능
- 공공 API 연동 경험을 포트폴리오로 보여줄 수 있음

### 6.6 Notification

MVP 단계별로 나누어 구현한다.

MVP 1차:

- 앱 내 오늘 추천 화면
- 알림 발송 대상 생성
- 알림 로그 저장

MVP 1.5:

- 웹 푸시 또는 FCM 연동

MVP 2차:

- PWA 푸시 알림 안정화

## 7. MVP 범위

### 7.1 MVP 1차 포함 기능

- Google OAuth 로그인
- 사용자 기본 프로필 저장
- 집/직장 위치 등록
- 동 단위 위치 검색/입력
- 출근 시간 설정
- 퇴근 시간 설정
- 알림 시간 설정
- 추위/더위 민감도 설정
- 이동수단 설정
- 기상청 단기예보 API 연동
- 오늘 날씨 조회
- 출근/퇴근 시간대 날씨 분석
- 룰 기반 옷차림 추천
- 준비물 추천
- 오늘 추천 결과 저장
- 오늘 추천 화면 제공
- 추천 피드백 등록
- 알림 발송 대상 생성
- 알림 로그 저장
- 날씨/옷차림 유형별 고정 이미지 표시

### 7.2 MVP 1차 제외 기능

- 실제 웹 푸시 발송
- FCM 연동
- 생리주기 반영
- 가족 프로필
- 옷장 등록
- AI 추천
- 캐릭터 커스터마이징
- 지도 기반 위치 선택
- 현재 위치 자동 감지
- 결제/구독
- 앱스토어/플레이스토어 배포

## 8. 사용자 입력 정보

### 8.1 필수 입력

| 항목 | 설명 |
| --- | --- |
| 닉네임 | 사용자 표시명 |
| 집 위치 | 동 단위 입력 |
| 직장 위치 | 동 단위 입력 |
| 출근 시간 | 출근길 날씨 기준 |
| 퇴근 시간 | 퇴근길 날씨 기준 |
| 알림 시간 | 아침 정기 알림 기준 |
| 추위 민감도 | 추위를 많이 타는지 |
| 더위 민감도 | 더위를 많이 타는지 |
| 이동수단 | 도보/대중교통/자차 등 |

### 8.2 선택 입력

| 항목 | 설명 |
| --- | --- |
| 말투 설정 | 정보형/추천형/캐릭터형 |
| 변경 알림 설정 | 중요할 때만/비·눈만/받지 않기 |
| 컨디션 | 2차 기능 |
| 생리주기 | 2차 기능 |
| 가족 프로필 | 2차 기능 |

## 9. 말투 설정

사용자는 추천 문구의 말투를 선택할 수 있다.

### 9.1 정보형

오늘은 체감온도가 낮아 얇은 외투를 권장합니다.

### 9.2 추천형

오늘은 생각보다 쌀쌀해서 얇은 외투 하나 챙기는 게 좋아요.

### 9.3 캐릭터형

오늘 바람이 꽤 불어요! 바람막이 챙기면 딱 좋아요.

MVP 기본값은 캐릭터형으로 설정한다.

## 10. 화면 구성

### 10.1 로그인 화면

목적:

- Google OAuth 로그인 제공

구성 요소:

- 서비스 로고
- 서비스 한 줄 설명
- Google 로그인 버튼

### 10.2 온보딩 화면

목적:

- 추천 생성을 위한 기본 사용자 설정 수집

입력 항목:

- 닉네임
- 집 위치
- 직장 위치
- 출근 시간
- 퇴근 시간
- 알림 시간
- 추위 민감도
- 더위 민감도
- 이동수단
- 말투 설정

### 10.3 오늘 추천 화면

목적:

- 사용자가 아침에 가장 먼저 확인하는 메인 화면

구성 요소:

- 오늘 한 줄 요약
- 캐릭터/고정 이미지
- 현재 날씨
- 출근길 날씨
- 퇴근길 날씨
- 추천 옷차림
- 챙길 것
- 추천 이유
- 피드백 버튼

예시:

```text
오늘은 바람 때문에 생각보다 쌀쌀해요!
상의: 얇은 니트
외투: 바람막이
준비물: 작은 우산

이유:
출근길은 괜찮지만 퇴근길에 기온이 내려가고,
바람이 강해서 체감온도가 낮아요.
```

### 10.4 피드백 화면

목적:

- 추천 정확도 개선을 위한 사용자 피드백 수집

입력 항목:

- 오늘 옷차림 어땠나요?
  - 추웠어요
  - 딱 좋았어요
  - 더웠어요
- 우산 추천은 어땠나요?
  - 필요했어요
  - 필요 없었어요

### 10.5 설정 화면

목적:

- 사용자 설정 변경

설정 항목:

- 집 위치
- 직장 위치
- 출근 시간
- 퇴근 시간
- 알림 시간
- 추위 민감도
- 더위 민감도
- 이동수단
- 말투 설정
- 변경 알림 설정

## 11. 화면 흐름도

```text
[로그인 화면]
      ↓
[Google OAuth 로그인]
      ↓
[온보딩 여부 확인]
      ↓
온보딩 미완료 → [온보딩 화면] → [오늘 추천 화면]
온보딩 완료   → [오늘 추천 화면]
      ↓
[추천 상세 확인]
      ↓
[피드백 등록]
      ↓
[설정 변경 가능]
```

## 12. 주요 도메인 모델

### 12.1 User

사용자 계정 정보

주요 속성:

- id
- email
- nickname
- provider
- providerId
- createdAt
- updatedAt

### 12.2 UserProfile

추천 생성을 위한 사용자 설정 정보

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

### 12.3 Location

사용자의 집/직장 위치 정보

주요 속성:

- id
- userId
- type
- sido
- sigungu
- dong
- nx
- ny

### 12.4 WeatherForecast

날씨 조회 결과

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
- dustLevel
- uvIndex
- rawData

### 12.5 OutfitRecommendation

추천 결과

주요 속성:

- id
- userId
- targetDate
- summaryMessage
- topRecommendation
- outerRecommendation
- itemRecommendation
- reason
- weatherSnapshot
- recommendationType
- createdAt

### 12.6 Feedback

추천 피드백

주요 속성:

- id
- recommendationId
- userId
- temperatureFeedback
- rainFeedback
- comment
- createdAt

### 12.7 NotificationLog

알림 로그

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

## 13. DB 테이블 설계 초안

### 13.1 users

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 13.2 user_profiles

```sql
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    cold_sensitivity INT NOT NULL DEFAULT 3,
    heat_sensitivity INT NOT NULL DEFAULT 3,
    commute_time TIME NOT NULL,
    leave_work_time TIME NOT NULL,
    notification_time TIME NOT NULL,
    transport_type VARCHAR(50) NOT NULL,
    message_tone VARCHAR(50) NOT NULL DEFAULT 'CHARACTER',
    change_alert_option VARCHAR(50) NOT NULL DEFAULT 'IMPORTANT_ONLY',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 13.3 locations

```sql
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    location_type VARCHAR(50) NOT NULL,
    sido VARCHAR(100) NOT NULL,
    sigungu VARCHAR(100) NOT NULL,
    dong VARCHAR(100) NOT NULL,
    nx INT,
    ny INT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

`location_type` 예시:

- HOME
- WORK

### 13.4 weather_forecasts

```sql
CREATE TABLE weather_forecasts (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT NOT NULL REFERENCES locations(id),
    forecast_date DATE NOT NULL,
    forecast_time TIME NOT NULL,
    temperature DECIMAL(5,2),
    feels_like_temperature DECIMAL(5,2),
    min_temperature DECIMAL(5,2),
    max_temperature DECIMAL(5,2),
    rain_probability INT,
    precipitation_type VARCHAR(50),
    wind_speed DECIMAL(5,2),
    humidity INT,
    dust_level VARCHAR(50),
    uv_index VARCHAR(50),
    raw_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 13.5 outfit_recommendations

```sql
CREATE TABLE outfit_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_date DATE NOT NULL,
    summary_message TEXT NOT NULL,
    top_recommendation VARCHAR(255),
    outer_recommendation VARCHAR(255),
    item_recommendation VARCHAR(255),
    reason TEXT,
    recommendation_type VARCHAR(50),
    weather_snapshot JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 13.6 feedbacks

```sql
CREATE TABLE feedbacks (
    id BIGSERIAL PRIMARY KEY,
    recommendation_id BIGINT NOT NULL REFERENCES outfit_recommendations(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    temperature_feedback VARCHAR(50),
    rain_feedback VARCHAR(50),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

`temperature_feedback` 예시:

- COLD
- GOOD
- HOT

`rain_feedback` 예시:

- NEEDED
- NOT_NEEDED

### 13.7 notification_logs

```sql
CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    recommendation_id BIGINT REFERENCES outfit_recommendations(id),
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

`notification_type` 예시:

- MORNING_REGULAR
- WEATHER_CHANGE

`status` 예시:

- PENDING
- SENT
- FAILED
- SKIPPED

## 14. 추천 로직 설계

### 14.1 추천 기준 데이터

추천 로직은 다음 데이터를 기반으로 한다.

- 기온
- 체감온도
- 최저기온
- 최고기온
- 일교차
- 강수확률
- 강수 시간대
- 풍속
- 미세먼지
- 출근 시간대 날씨
- 퇴근 시간대 날씨
- 추위 민감도
- 더위 민감도
- 이동수단

### 14.2 기본 추천 흐름

1. 사용자 프로필 조회
2. 집/직장 위치 조회
3. 출근/퇴근 시간 기준 날씨 조회
4. 날씨 조건 분석
5. 사용자 민감도 반영
6. 최종 체감 기준 온도 계산
7. 옷차림 추천 생성
8. 준비물 추천 생성
9. 추천 이유 생성
10. 추천 결과 저장

### 14.3 체감 기준 온도 보정

예시:

```text
기본 체감온도: 15도
바람 강함: -2도
추위를 많이 탐: -2도
도보 이동: -1도
퇴근길 기온 하락: -1도
최종 추천 기준 온도: 10도
```

### 14.4 옷차림 추천 예시

| 기준 체감온도 | 추천 |
| --- | --- |
| 28도 이상 | 반팔, 얇은 하의, 자외선 주의 |
| 23~27도 | 반팔 또는 얇은 셔츠 |
| 20~22도 | 얇은 긴팔, 셔츠 |
| 17~19도 | 얇은 니트, 가디건 |
| 12~16도 | 니트, 바람막이, 자켓 |
| 8~11도 | 코트, 두꺼운 가디건 |
| 4~7도 | 패딩 또는 두꺼운 코트 |
| 3도 이하 | 두꺼운 패딩, 목도리, 장갑 |

### 14.5 준비물 추천

| 조건 | 추천 준비물 |
| --- | --- |
| 강수확률 60% 이상 | 우산 |
| 퇴근 시간 비 예보 | 작은 우산 |
| 눈 예보 | 미끄럼 주의 신발 |
| 미세먼지 나쁨 이상 | 마스크 |
| 자외선 높음 | 선크림/모자 |
| 풍속 강함 | 바람막이 |

### 14.6 추천 문구 예시

캐릭터형:

```text
오늘은 바람이 꽤 불어요!
얇은 니트에 바람막이를 챙기면 딱 좋아요.
퇴근길에는 비가 올 수 있으니 작은 우산도 챙겨주세요~
```

추천형:

```text
오늘은 출근길보다 퇴근길이 더 쌀쌀해요.
얇은 외투를 챙기는 게 좋아요.
```

정보형:

```text
퇴근 시간대 체감온도가 낮아질 것으로 예상됩니다.
얇은 외투 착용을 권장합니다.
```

## 15. 알림 정책

### 15.1 알림 종류

아침 정기 알림:

- 사용자가 설정한 시간에 하루 1회 생성된다.
- MVP 1차에서는 실제 푸시를 보내지 않고 알림 로그만 생성한다.

날씨 변경 알림:

- 아침 정기 알림 이후 날씨 예보가 변경되었을 때, 사용자의 행동 변화가 필요한 경우에만 생성된다.
- MVP 1.5 또는 2차에서 구현한다.

### 15.2 변경 알림 발송 조건

변경 알림은 단순한 수치 변화가 아니라 추천 결과가 바뀌는 경우에만 발송한다.

- 비 안 옴 → 비 옴
- 우산 불필요 → 우산 필요
- 외투 불필요 → 외투 필요
- 퇴근길 기온 급락
- 강풍주의 수준으로 변경
- 눈 예보 추가
- 미세먼지 매우 나쁨으로 변경

### 15.3 변경 알림 제한

- 정기 알림: 하루 1회
- 변경 알림: 하루 최대 1회

### 15.4 변경 알림 설정 옵션

- 중요할 때만
- 비/눈만
- 받지 않기

### 15.5 알림 문구 정책

날씨 예보 변경 시 "정정"이 아니라 "업데이트"로 표현한다.

좋은 예:

```text
퇴근 시간 예보가 바뀌었어요.
비가 올 가능성이 높아졌으니 작은 우산을 챙기세요.
```

피해야 할 예:

```text
아까 알림이 틀렸어요.
```

## 16. 배치/스케줄링 구조

### 16.1 아침 정기 추천 생성

1. 현재 시간 기준 알림 대상 사용자 조회
2. 사용자 프로필 조회
3. 집/직장 위치 조회
4. 기상청 API 호출 또는 캐시 조회
5. 출근/퇴근 시간대 날씨 분석
6. 옷차림 추천 생성
7. 추천 결과 저장
8. 알림 로그 생성

MVP 1차에서는 실제 푸시를 보내지 않고 알림 로그를 생성한다.

### 16.2 날씨 변경 감지

MVP 1.5 또는 2차에서 구현한다.

1. 아침 추천 결과 조회
2. 최신 날씨 예보 재조회
3. 기존 추천 결과와 새 추천 결과 비교
4. 추천 결과가 바뀌었는지 판단
5. 변경 알림 조건 확인
6. 하루 발송 횟수 확인
7. 변경 알림 로그 생성
8. 실제 푸시 발송

## 17. API 명세 초안

### 17.1 Auth

Google OAuth 로그인:

```http
GET /oauth2/authorization/google
```

내 정보 조회:

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

### 17.2 Profile

온보딩 저장:

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

프로필 조회:

```http
GET /api/profile
```

프로필 수정:

```http
PUT /api/profile
```

### 17.3 Location

동 검색:

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

### 17.4 Weather

오늘 날씨 조회:

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
    "windSpeed": 4.2,
    "dustLevel": "NORMAL"
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

### 17.5 Recommendation

오늘 추천 조회:

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

오늘 추천 생성:

```http
POST /api/recommendations/today
```

### 17.6 Feedback

피드백 등록:

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

### 17.7 Notification

알림 로그 조회:

```http
GET /api/notifications
```

오늘 알림 로그 조회:

```http
GET /api/notifications/today
```

## 18. 추천 엔진 클래스 구조 예시

```text
recommendation
 ├── RecommendationController
 ├── RecommendationService
 ├── RecommendationRuleEngine
 ├── WeatherConditionAnalyzer
 ├── OutfitSelector
 ├── ItemSelector
 ├── RecommendationMessageGenerator
 └── dto
```

### 18.1 RecommendationService 역할

- 사용자 프로필 조회
- 날씨 데이터 조회
- 추천 엔진 호출
- 추천 결과 저장
- 응답 DTO 생성

### 18.2 RecommendationRuleEngine 역할

- 날씨 조건과 사용자 설정을 기반으로 추천 기준 온도 계산
- 외투 필요 여부 판단
- 우산 필요 여부 판단
- 미세먼지/자외선/강풍 여부 판단

### 18.3 RecommendationMessageGenerator 역할

- 사용자의 말투 설정에 따라 추천 문구 생성
- 정보형/추천형/캐릭터형 문구 분리

## 19. 백엔드 패키지 구조 초안

```text
src/main/java/com/weatherfit
 ├── WeatherFitApplication.java
 ├── auth
 │   ├── controller
 │   ├── service
 │   ├── domain
 │   └── config
 ├── user
 │   ├── controller
 │   ├── service
 │   ├── domain
 │   ├── repository
 │   └── dto
 ├── profile
 │   ├── controller
 │   ├── service
 │   ├── domain
 │   ├── repository
 │   └── dto
 ├── location
 │   ├── controller
 │   ├── service
 │   ├── domain
 │   ├── repository
 │   └── dto
 ├── weather
 │   ├── client
 │   ├── service
 │   ├── domain
 │   ├── repository
 │   └── dto
 ├── recommendation
 │   ├── controller
 │   ├── service
 │   ├── domain
 │   ├── repository
 │   ├── engine
 │   └── dto
 ├── feedback
 │   ├── controller
 │   ├── service
 │   ├── domain
 │   ├── repository
 │   └── dto
 ├── notification
 │   ├── scheduler
 │   ├── service
 │   ├── domain
 │   ├── repository
 │   └── dto
 └── global
     ├── config
     ├── error
     ├── security
     └── util
```

## 20. 프론트엔드 구조 초안

```text
src
 ├── main.tsx
 ├── App.tsx
 ├── pages
 │   ├── LoginPage.tsx
 │   ├── OnboardingPage.tsx
 │   ├── TodayRecommendationPage.tsx
 │   ├── FeedbackPage.tsx
 │   └── SettingsPage.tsx
 ├── components
 │   ├── WeatherSummaryCard.tsx
 │   ├── OutfitRecommendationCard.tsx
 │   ├── CharacterImage.tsx
 │   ├── FeedbackButtons.tsx
 │   └── BottomNavigation.tsx
 ├── api
 │   ├── authApi.ts
 │   ├── profileApi.ts
 │   ├── weatherApi.ts
 │   ├── recommendationApi.ts
 │   └── feedbackApi.ts
 ├── hooks
 │   ├── useTodayRecommendation.ts
 │   └── useProfile.ts
 ├── routes
 │   └── Router.tsx
 ├── types
 │   ├── user.ts
 │   ├── profile.ts
 │   ├── weather.ts
 │   └── recommendation.ts
 └── pwa
     └── serviceWorkerRegistration.ts
```

## 21. PWA 요구사항

MVP에서는 PWA 기본 설정을 적용한다.

포함 항목:

- manifest 설정
- 앱 이름
- 앱 아이콘
- theme color
- mobile display standalone
- service worker 기본 구성

목적:

- 모바일 홈 화면에 앱처럼 추가 가능
- 브라우저 주소창 없이 앱처럼 실행 가능
- 지인 테스트 시 스토어 배포 없이 링크 공유 가능
- 추후 웹 푸시 확장 기반 확보

## 22. 이미지/캐릭터 정책

MVP에서는 복잡한 캐릭터 커스터마이징을 하지 않는다.

MVP:

- 날씨/옷차림 유형별 고정 이미지 사용

예시:

- SUNNY_LIGHT
- RAIN_UMBRELLA
- WINDY_LIGHT_OUTER
- COLD_PADDING
- DUST_MASK

2차:

- 사용자가 캐릭터 타입 선택

3차:

- 캐릭터 커스터마이징

## 23. 개인정보/민감정보 정책

### 23.1 MVP에서 다루는 정보

- 이메일
- 닉네임
- 집/직장 위치
- 출근/퇴근 시간
- 알림 시간
- 추위/더위 민감도
- 이동수단
- 추천 피드백

### 23.2 MVP에서 다루지 않는 정보

- 생리주기
- 건강 상태
- 지병
- 상세 주소
- 결제 정보

### 23.3 위치 정보 처리

MVP에서는 정확한 주소가 아니라 동 단위 위치를 저장한다.

예시:

```text
서울특별시 강남구 역삼동
```

정확한 도로명 주소나 상세 주소는 저장하지 않는다.

### 23.4 생리주기 기능

생리주기 기능은 2차 이후 선택 기능으로만 고려한다.

원칙:

- 기본 기능에 포함하지 않는다.
- 사용자가 직접 활성화한 경우에만 사용한다.
- 민감정보 동의를 별도로 받는다.
- 의료 조언처럼 표현하지 않는다.

좋은 표현:

```text
오늘은 평소보다 따뜻하게 입는 걸 추천해요.
```

피해야 할 표현:

```text
생리통이 심해질 수 있으니 이렇게 하세요.
```

## 24. MVP 개발 단계

### Phase 1. 프로젝트 초기 세팅

목표:

- 백엔드/프론트 프로젝트 생성
- DB 연결
- 기본 환경 구성

작업:

- Spring Boot 프로젝트 생성
- React + Vite 프로젝트 생성
- PostgreSQL Docker 설정
- 환경변수 관리
- 기본 패키지 구조 생성
- GitHub 레포지토리 구성

### Phase 2. 인증/사용자

목표:

- Google OAuth 로그인
- 사용자 정보 저장

작업:

- Spring Security 설정
- Google OAuth 설정
- 로그인 성공 후 사용자 저장
- 내 정보 조회 API
- 프론트 로그인 화면 구현

### Phase 3. 온보딩/프로필

목표:

- 추천 생성을 위한 사용자 설정 저장

작업:

- UserProfile 테이블 생성
- Location 테이블 생성
- 온보딩 API 구현
- 프로필 조회/수정 API 구현
- 프론트 온보딩 화면 구현
- 동 단위 위치 입력 UI 구현

### Phase 4. 날씨 API 연동

목표:

- 기상청 단기예보 API 연동

작업:

- 기상청 API 클라이언트 구현
- 동 → 예보 좌표 매핑
- 날씨 데이터 파싱
- 출근/퇴근 시간대 날씨 추출
- 날씨 조회 API 구현
- weather_forecasts 저장 구조 구현

### Phase 5. 추천 로직

목표:

- 룰 기반 옷차림 추천 생성

작업:

- RecommendationRuleEngine 구현
- 체감 기준 온도 계산
- 우산 필요 여부 판단
- 외투 필요 여부 판단
- 준비물 추천
- 추천 문구 생성
- 추천 결과 저장
- 오늘 추천 조회 API 구현

### Phase 6. 오늘 추천 화면

목표:

- 사용자가 오늘 추천을 확인할 수 있다.

작업:

- TodayRecommendationPage 구현
- 날씨 요약 카드
- 캐릭터 이미지
- 옷차림 추천 카드
- 준비물 추천
- 추천 이유 표시
- 반응형 모바일 UI

### Phase 7. 피드백

목표:

- 사용자가 오늘 추천이 맞았는지 피드백할 수 있다.

작업:

- Feedback 테이블 생성
- 피드백 등록 API 구현
- 피드백 버튼 UI 구현
- 피드백 등록 후 완료 메시지 표시

### Phase 8. 알림 로그

목표:

- 사용자별 아침 알림 대상과 알림 내용을 생성한다.

작업:

- NotificationLog 테이블 생성
- 아침 알림 대상 조회 로직 구현
- 추천 결과 기반 알림 메시지 생성
- 알림 로그 저장
- 알림 로그 조회 API 구현

### Phase 9. PWA 설정

목표:

- 모바일에서 앱처럼 사용할 수 있게 한다.

작업:

- manifest 설정
- 앱 아이콘 설정
- theme color 설정
- service worker 설정
- 홈 화면 추가 테스트

## 25. MVP 이후 확장 계획

### Phase 1.5

목표:

- 실제 알림 기능을 도입한다.

기능:

- 웹 푸시 또는 FCM 연동
- 브라우저 알림 권한 요청
- 푸시 구독 정보 저장
- 알림 발송 실패 처리
- 알림 발송 성공/실패 로그 기록

### Phase 2

목표:

- 날씨 변경 감지 기반 스마트 알림 구현

기능:

- 최신 날씨 재조회
- 기존 추천 결과와 비교
- 우산/외투 필요 여부 변경 감지
- 하루 최대 1회 변경 알림
- 변경 알림 옵션 적용

### Phase 3

목표:

- 피드백 기반 개인화 강화

기능:

- 사용자별 체감온도 보정값 계산
- "추웠어요" 피드백 누적 시 더 따뜻하게 추천
- "더웠어요" 피드백 누적 시 더 가볍게 추천
- 추천 정확도 개선

### Phase 4

목표:

- 컨디션 기능 도입

기능:

- 전날 밤 또는 아침 컨디션 입력
- 평소와 같음
- 몸이 으슬으슬함
- 피곤함
- 컨디션 기반 따뜻한 추천 보정

### Phase 5

목표:

- 생리주기 선택 기능 도입

기능:

- 사용자가 명시적으로 활성화한 경우만 사용
- 민감정보 동의
- 생리 전/중 따뜻한 추천 보정
- 의료 조언 표현 금지

### Phase 6

목표:

- 가족 프로필 기능 도입

기능:

- 가족 프로필 추가
- 엄마/아빠/아이 등 프로필별 추천
- 프로필별 추위/더위 민감도 설정
- 프로필별 알림 시간 설정

### Phase 7

목표:

- 캐릭터 기능 강화

기능:

- 사용자 캐릭터 선택
- 날씨별 캐릭터 이미지
- 옷차림 조합별 이미지
- 캐릭터 커스터마이징

### Phase 8

목표:

- 상용화 가능성 검토

기능:

- 광고 제거
- 캐릭터 아이템
- 가족 프로필 추가
- 일주일 옷차림 플래너
- 구독 모델 검토

## 26. 비기능 요구사항

### 26.1 성능

- 오늘 추천 화면은 2초 이내 응답을 목표로 한다.
- 날씨 API 응답이 느릴 경우 캐시된 데이터를 활용할 수 있도록 확장한다.
- 동일 지역 날씨 데이터 중복 호출을 줄인다.

### 26.2 안정성

- 기상청 API 장애 시 기본 안내 메시지를 제공한다.
- 알림 생성 실패 시 실패 로그를 남긴다.
- 추천 생성 실패 시 사용자에게 재시도 안내를 제공한다.

### 26.3 보안

- Google OAuth 기반 인증 사용
- 사용자별 데이터 접근 제한
- 다른 사용자의 프로필/추천/피드백 조회 불가
- 민감정보는 MVP에서 저장하지 않음

### 26.4 확장성

- 추천 로직은 별도 엔진 클래스로 분리한다.
- 알림 생성과 실제 발송은 분리한다.
- 날씨 API 클라이언트는 교체 가능하게 설계한다.
- 추후 Kotlin 모듈 도입 가능성을 고려한다.

## 27. 전체 아키텍처

```text
[React + Vite + PWA]
        ↓ REST API
[Spring Boot Backend]
        ↓
[PostgreSQL]
```

Spring Boot 내부 구성:

- Auth Module
- User/Profile Module
- Location Module
- Weather Module
- Recommendation Module
- Feedback Module
- Notification Module
- Scheduler Module

외부 연동:

- Google OAuth
- 기상청 단기예보 API
- 추후 Web Push / FCM

## 28. 데이터 흐름

### 28.1 오늘 추천 조회 흐름

```text
사용자 앱 접속
→ 로그인 확인
→ 오늘 추천 조회 API 호출
→ 오늘 추천이 이미 있으면 반환
→ 없으면 사용자 프로필 조회
→ 위치 조회
→ 날씨 API 조회
→ 추천 로직 실행
→ 추천 결과 저장
→ 추천 결과 반환
```

### 28.2 아침 알림 로그 생성 흐름

```text
스케줄러 실행
→ 현재 시간 기준 알림 대상 사용자 조회
→ 사용자별 오늘 추천 생성/조회
→ 알림 문구 생성
→ notification_logs 저장
→ MVP 1차에서는 실제 발송하지 않음
```

### 28.3 피드백 저장 흐름

```text
사용자 피드백 버튼 클릭
→ 피드백 등록 API 호출
→ recommendation_id 기준 피드백 저장
→ 추후 개인화 보정 데이터로 활용
```

## 29. Codex 개발 지시 문서

### 29.1 개발 목표

이 프로젝트는 "오늘 뭐입지?"라는 개인화 날씨 기반 옷차림 추천 서비스의 MVP이다.

MVP 1차에서는 실제 푸시 알림까지 구현하지 않고, 다음 기능을 우선 구현한다.

- Google OAuth 로그인
- 사용자 온보딩
- 집/직장 위치 저장
- 기상청 단기예보 API 연동
- 출근/퇴근 시간대 날씨 분석
- 룰 기반 옷차림 추천
- 오늘 추천 화면
- 피드백 저장
- 알림 로그 생성
- React + PWA 기본 설정

### 29.2 기술 스택

Backend:

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL

Frontend:

- React
- Vite
- TypeScript
- PWA

Auth:

- Google OAuth

Weather:

- 기상청 단기예보 API

### 29.3 개발 원칙

- MVP 범위 밖 기능을 먼저 구현하지 않는다.
- 추천 로직은 하드코딩을 최소화하고 별도 엔진 클래스로 분리한다.
- 알림 생성과 알림 발송은 분리한다.
- 날씨 API 응답 원본은 JSONB로 저장할 수 있게 한다.
- 사용자별 데이터 접근 제어를 지킨다.
- 컨트롤러, 서비스, 도메인, 리포지토리 역할을 분리한다.
- 프론트는 모바일 사용성을 우선한다.

### 29.4 우선 구현 순서

1. 프로젝트 초기 세팅
2. DB 연결
3. Google OAuth 로그인
4. 사용자/프로필/위치 도메인
5. 온보딩 API
6. 온보딩 화면
7. 날씨 API 연동
8. 추천 로직
9. 오늘 추천 API
10. 오늘 추천 화면
11. 피드백 API
12. 피드백 UI
13. 알림 로그 생성
14. PWA 설정

### 29.5 MVP에서 하지 말 것

- 결제 기능 구현하지 않기
- 생리주기 기능 구현하지 않기
- 가족 프로필 구현하지 않기
- 옷장 등록 구현하지 않기
- AI 추천 구현하지 않기
- 지도 선택 기능 구현하지 않기
- 실제 푸시 알림은 MVP 1차에서 구현하지 않기
- 캐릭터 커스터마이징 구현하지 않기

## 30. 향후 검증 기준

### 30.1 개인 사용 기준

- 2주 동안 매일 아침 확인하는가?
- 추천이 실제 옷차림 결정에 도움이 되는가?
- 우산/외투 추천이 맞는가?
- 피드백을 남기게 되는가?

### 30.2 가족 테스트 기준

- 엄마도 문구를 쉽게 이해하는가?
- "오늘 뭐 입어야 하는지" 바로 알 수 있는가?
- 알림 문구가 부담스럽지 않은가?

### 30.3 지인 테스트 기준

- 5~10명이 실제로 사용해보는가?
- 내일도 받고 싶다고 느끼는가?
- 추천 정확도에 대한 피드백이 긍정적인가?
- 너무 많은 알림으로 느껴지지 않는가?

## 31. 최종 MVP 정의

MVP의 최종 목표는 다음과 같다.

> 사용자가 매일 아침 앱을 열었을 때, 출근길과 퇴근길 날씨를 바탕으로 오늘 입을 옷과 챙길 물건을 5초 안에 결정할 수 있게 한다.

## 32. 현재 확정 사항 요약

| 항목 | 내용 |
| --- | --- |
| 서비스명 | 오늘 뭐입지? |
| 레포명 후보 | weather-fit |
| 목표 | 개인 사용 MVP + 포트폴리오 + 추후 배포 가능성 |
| Frontend | React + Vite + PWA |
| Backend | Java + Spring Boot |
| Database | PostgreSQL |
| Login | Google OAuth |
| Weather API | 기상청 단기예보 API |
| Location | 집 + 직장, 동 단위 입력 |
| Recommendation | 룰 기반 |
| Notification MVP 1차 | 알림 로그 생성 |
| Notification MVP 1.5 | 웹 푸시 또는 FCM |
| Character MVP | 날씨/옷차림 유형별 고정 이미지 |
| Feedback | 추웠어요/딱 좋았어요/더웠어요, 우산 필요/불필요 |
| Personalization MVP | 추위/더위 민감도 + 출근/퇴근 시간 + 이동수단 |
| Personalization Later | 피드백 기반 보정, 컨디션, 생리주기, 가족 프로필 |
