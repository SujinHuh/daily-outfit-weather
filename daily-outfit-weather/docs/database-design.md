# Database Design

PostgreSQL 기준 DB 설계 초안입니다. DB 스키마 변경은 Flyway SQL migration으로 관리합니다.

## 설계 원칙

- 사용자별 데이터 접근 제어를 전제로 설계합니다.
- 추천 생성 당시의 날씨 조건과 판단 근거는 JSONB로 저장할 수 있게 합니다.
- 사용자당 프로필은 하나만 허용합니다.
- 사용자당 집/직장 위치는 각각 하나만 허용합니다.
- 사용자당 하루 추천 결과는 기본적으로 하나만 허용합니다.

## users

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id)
);
```

## user_profiles

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
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_profiles_user_id UNIQUE (user_id)
);
```

`change_alert_option`은 MVP 1차에서 설정값 저장 용도로만 사용합니다. 실제 날씨 변경 알림 생성 로직은 MVP 1.5 또는 2차에서 구현합니다.

## locations

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
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_locations_user_type UNIQUE (user_id, location_type)
);
```

`location_type` 예시:

- HOME
- WORK

## weather_forecasts

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
    raw_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

주의:

- 미세먼지와 자외선은 기상청 단기예보 API만으로 직접 처리하지 않으므로 1차 테이블에서 제외합니다.
- 후순위에서 별도 API를 붙일 경우 컬럼 추가 또는 별도 테이블을 검토합니다.

## outfit_recommendations

```sql
CREATE TABLE outfit_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_date DATE NOT NULL,
    summary_message TEXT NOT NULL,
    top_recommendation VARCHAR(255),
    outer_recommendation VARCHAR(255),
    item_recommendation VARCHAR(255),
    character_image_type VARCHAR(100),
    reason TEXT,
    recommendation_type VARCHAR(50),
    weather_snapshot JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_outfit_recommendations_user_date UNIQUE (user_id, target_date)
);
```

`character_image_type`은 추천 당시 어떤 이미지 타입이 선택됐는지 기록하기 위해 저장합니다.

## feedbacks

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

## notification_logs

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

## Migration 관리

- DB 마이그레이션 도구는 Flyway를 사용합니다.
- 마이그레이션 파일은 Spring Boot 기본 관례에 맞춰 `src/main/resources/db/migration` 아래에 둡니다.
- 파일명은 `V1__create_users.sql`처럼 버전과 설명을 포함합니다.

예시:

```text
src/main/resources/db/migration
├── V1__create_users.sql
├── V2__create_user_profiles.sql
├── V3__create_locations.sql
└── V4__create_weather_forecasts.sql
```

## Enum 저장 전략

- MVP 1차에서는 enum 값 검증을 애플리케이션 레벨에서 먼저 처리합니다.
- DB check constraint는 enum 변경 빈도가 낮아지고 마이그레이션 정책이 안정된 뒤 도입을 재검토합니다.

## 테스트 DB 전략

- 테스트 DB는 Testcontainers PostgreSQL을 사용합니다.
- Flyway migration과 JPA 매핑은 실제 PostgreSQL 기준으로 검증합니다.
- 단위 테스트는 DB 없이 수행합니다.
