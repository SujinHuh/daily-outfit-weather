# Architecture

`daily-outfit-weather`의 전체 구조와 백엔드/프론트엔드 패키지 기준을 정리한 대표 아키텍처 문서입니다.

## 전체 아키텍처

이 프로젝트는 MVP 단계에서 **Modular Monolith + Layered Architecture**로 구현합니다.

- 애플리케이션은 하나의 Spring Boot 백엔드로 유지합니다.
- 내부 패키지는 도메인별 모듈처럼 나눕니다.
- 각 모듈 안에서는 controller, service, domain, repository, dto 계층을 분리합니다.
- MSA는 MVP에서 사용하지 않습니다.
- 추후 트래픽, 배포 독립성, 장애 격리 필요가 명확해지면 weather, recommendation, notification 같은 모듈을 별도 서비스로 분리할 수 있게 경계를 유지합니다.

```text
[React + Vite + PWA]
        ↓ REST API
[Spring Boot Backend]
        ↓
[PostgreSQL]
```

외부 연동:

- Google OAuth
- 기상청 단기예보 API
- 추후 Web Push / FCM

## 백엔드 모듈

모듈은 하나의 애플리케이션 안에서 패키지 경계로 나눕니다. 모듈 간 직접 의존은 최소화하고, 다른 모듈의 내부 구현보다 service 계층 또는 명시적인 DTO를 통해 협력합니다.

- Auth Module
- User/Profile Module
- Location Module
- Weather Module
- Recommendation Module
- Feedback Module
- Notification Module
- Scheduler Module

## 백엔드 패키지 구조 초안

구현 기준 패키지명은 `com.dailyoutfitweather`입니다. 원본 기획서의 `com.weatherfit`은 legacy 이름입니다.

각 모듈의 기본 계층 규칙:

- `controller`: HTTP 요청/응답 처리
- `service`: 유스케이스 orchestration
- `domain`: 도메인 모델과 핵심 상태
- `repository`: 영속성 접근
- `dto`: 요청/응답 데이터 전달 객체
- `client`: 외부 API 호출 어댑터
- `engine`: 추천 계산처럼 독립적인 도메인 로직

```text
src/main/java/com/dailyoutfitweather
 ├── DailyOutfitWeatherApplication.java
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

## 프론트엔드 구조 초안

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

## 데이터 흐름

### 오늘 추천 조회

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

### 아침 알림 로그 생성

```text
스케줄러 실행
→ 현재 시간 기준 알림 대상 사용자 조회
→ 사용자별 오늘 추천 생성/조회
→ 알림 문구 생성
→ notification_logs 저장
→ MVP 1차에서는 실제 발송하지 않음
```

### 피드백 저장

```text
사용자 피드백 버튼 클릭
→ 피드백 등록 API 호출
→ recommendation_id 기준 피드백 저장
→ 추후 개인화 보정 데이터로 활용
```

## 비기능 요구사항

성능:

- 오늘 추천 화면은 2초 이내 응답을 목표로 합니다.
- 날씨 API 응답이 느릴 경우 캐시된 데이터를 활용할 수 있도록 확장합니다.
- 동일 지역 날씨 데이터 중복 호출을 줄입니다.

안정성:

- 기상청 API 장애 시 기본 안내 메시지를 제공합니다.
- 알림 생성 실패 시 실패 로그를 남깁니다.
- 추천 생성 실패 시 사용자에게 재시도 안내를 제공합니다.

보안:

- Google OAuth 기반 인증 사용
- 사용자별 데이터 접근 제한
- 다른 사용자의 프로필/추천/피드백 조회 불가
- 민감정보는 MVP에서 저장하지 않음

확장성:

- 추천 로직은 별도 엔진 클래스로 분리합니다.
- 알림 생성과 실제 발송은 분리합니다.
- 날씨 API 클라이언트는 교체 가능하게 설계합니다.
- 추후 Kotlin 모듈 도입 가능성을 고려합니다.
