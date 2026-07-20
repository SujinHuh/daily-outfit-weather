# GCP 배포 및 개인/가족 전용 모바일 PWA 배포 가이드 (GCP Deployment Guide)

이 문서는 GCP (Google Cloud Platform) Compute Engine VM 환경에 **오늘 뭐입지? (Daily Outfit Weather)** 서비스를 배포하고, 본인 및 가족들의 스마트폰에서 모바일 PWA(웹 앱)로 사용하기 위한 배포 단계별 로드맵과 점검 가이드입니다.

---

## 1. 배포 목적 및 토폴로지 (Deployment Topology)

* **배포 목적**: 본인 및 가족 전용 스마트폰 서비스 제공 (개인/가족 전용 PWA 서비스)
* **배포 환경**: GCP Compute Engine (Linux VM 인스턴스)
* **서비스 구성 (Docker Compose)**:
  * `frontend`: Nginx 1.27 기반 정적 파일 서빙 + Reverse Proxy (포트 80)
  * `backend`: Spring Boot 3.x / Java 21 (포트 8080 internal)
  * `postgres`: PostgreSQL 16 Alpine (볼륨 영속화 `postgres-data`)

```text
[스마트폰 브라우저 (PWA)]
        ↓ HTTP (포트 80)
[GCP Compute Engine External IP]
        ↓
[Frontend: Nginx Container]
  ├── /             -> React SPA 정적 파일
  └── /api/, /oauth2/, /login/ -> [Backend: Spring Boot Container (8080)]
                                            ↓
                                  [PostgreSQL Database (5432)]
```

---

## 2. 배포 단계별 로드맵 (Deployment Roadmap & Checklist)

### 📌 1단계: 로컬 준비 및 배포 설정 보완 (`완료`)
- [x] `docker-compose.prod.yml`에 컨테이너 로그 용량 제한 설정 (`json-file`, `max-size: 10m`, `max-file: 3`) 반영
- [x] `ProductionConfigurationValidator`에 의한 필수 환경변수 구체화 검증 확인
- [x] GCP용 `.env.gcp.example` 템플릿 검증 완료

---

### 📌 2단계: GCP 서버(VM) 인프라 구축 (`사용자 진행`)
1. **Compute Engine VM 인스턴스 생성**:
   * 추천 머신 유형: `e2-small` 또는 `e2-micro`
   * OS: Ubuntu 22.04 LTS 또는 Debian
2. **외부 고정 IP (Static External IP) 예약**:
   * GCP 콘솔 -> `VPC 네트워크` -> `외부 IP 주소`에서 VM의 외부 IP를 고정(Static)으로 설정 (서버 재부팅 시 IP 변경 방지)
3. **VPC 방화벽 규칙 허용**:
   * `http-server` (포트 80 허용)
   * `https-server` (포트 443 허용 - 추후 SSL 적용 시)

---

### 📌 3단계: GCP 서버 환경 설정 & 컨테이너 기동 (`GCP VM 내부`)

#### ① Docker & Git 설치
```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2 git
sudo usermod -aG docker $USER
```

#### ② 프로젝트 클론 및 `.env` 파일 작성
GCP VM의 소스코드 루트 경로에 `.env` 파일을 작성합니다 (`.env.gcp.example` 참고).

```env
POSTGRES_DB=daily_outfit_weather
POSTGRES_USER=daily_outfit_weather
POSTGRES_PASSWORD=<강력한 DB 비밀번호>

SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=80
APP_FRONTEND_SUCCESS_URL=http://<GCP_VM_PUBLIC_IP>
APP_SECURITY_ALLOWED_ORIGINS=http://<GCP_VM_PUBLIC_IP>
APP_NOTIFICATION_GENERATE_DUE_TOKEN=<랜덤 비밀 토큰>

# HTTP 전용 배포 시 false (HTTPS 적용 시 true)
SESSION_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=lax

# Google OAuth 클라이언트 정보
GOOGLE_CLIENT_ID=<구글 콘솔 클라이언트 ID>
GOOGLE_CLIENT_SECRET=<구글 콘솔 클라이언트 보안 비밀>
GOOGLE_REDIRECT_URI=http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google

# 기상청 단기예보 API 인코딩 키 (Encoding Key)
KMA_SERVICE_KEY=<공공데이터포털 인코딩 인증키>
KMA_BASE_URL=http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0
KMA_CONNECT_TIMEOUT=2s
KMA_READ_TIMEOUT=3s
APP_WEATHER_FALLBACK_ENABLED=true
```

#### ③ Google Cloud Console OAuth 설정
1. [Google Cloud Console](https://console.cloud.google.com/) -> `API 및 서비스` -> `사용자 인증 정보` 이동
2. 사용 중인 OAuth 2.0 클라이언트 ID 선택
3. **승인된 리디렉션 URI**에 다음 주소 추가:
   * `http://<GCP_VM_PUBLIC_IP>/login/oauth2/code/google`
4. **OAuth 동의 화면** -> **테스트 사용자 (Test Users)**에 가족들의 구글 계정 이메일 추가 등록

#### ④ Docker Compose 기동
```bash
docker compose -f docker-compose.prod.yml up -d --build
```

#### ⑤ 헬스체크 및 정상 작동 확인
```bash
curl -i http://localhost/api/health
```

---

### 📌 4단계: 스마트폰(모바일 실기기) E2E 테스트 & PWA 적용

1. **스마트폰 접속 테스트**:
   * 본인 및 가족 스마트폰 브라우저(Safari / Chrome)에서 `http://<GCP_VM_PUBLIC_IP>` 접속
2. **Google 로그인 테스트**:
   * 구글 로그인 실행 후 등록된 가족 계정으로 정상 로그인되는지 확인
3. **온보딩 및 옷차림 추천 확인**:
   * 위치(집/직장), 출퇴근 시간, 체감 민감도 설정 후 추천 카드 및 카툰 캐릭터 표시 검증
4. **스마트폰 PWA 앱 설치 ("홈 화면에 추가")**:
   * **iOS (Safari)**: 공유 버튼 클릭 -> `홈 화면에 추가`
   * **Android (Chrome)**: 메뉴(⋮) 클릭 -> `홈 화면에 추가` 또는 `앱 설치`
   * 바탕화면 아이콘을 통해 독립 앱처럼 실행되는지 확인

---

## 3. 핵심 주의사항 및 트러블슈팅 가이드

* **`ProductionConfigurationValidator` 부팅 오류**:
  * `.env`에 `localhost`, `change-this`, `example.com` 등의 기본값이 들어가면 백엔드 앱이 부팅 과정에서 중단됩니다. 반드시 실제 값으로 대체하세요.
* **기상청 API 키 형식**:
  * 공공데이터포털의 `Decoding` 키가 아닌 **`Encoding` 키**를 사용해야 특수문자 파싱 에러가 발생하지 않습니다.
* **세션 쿠키 미전송 (로그인 풀림)**:
  * HTTP 환경에서 `SESSION_COOKIE_SECURE=true`로 설정하면 브라우저가 세션 쿠키를 저장하지 않아 로그인이 풀립니다. HTTP 배포 시에는 반드시 `false`로 유지하세요.
