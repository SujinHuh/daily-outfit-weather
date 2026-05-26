# Plan: 010 PWA Setup

## Phase 9. PWA 기본 설정

### 1. 환경 준비
- [ ] `vite-plugin-pwa` 라이브러리 설치
- [ ] PWA용 아이콘 준비 (favicon.svg 활용)

### 2. Vite 설정 수정
- [ ] `vite.config.ts`에 `VitePWA` 플러그인 설정 추가
- [ ] `manifest` 옵션 정의 (name, short_name, icons, theme_color, background_color, display 등)

### 3. 서비스 워커 등록 및 메타 태그
- [ ] `main.tsx` 또는 `App.tsx`에서 PWA 등록 로직 확인
- [ ] `index.html`에 모바일 앱 최적화를 위한 메타 태그 보강 (apple-mobile-web-app 등)

### 4. 검증
- [ ] `npm run build`를 통한 빌드 산출물 확인
- [ ] Lighthouse 또는 Chrome DevTools를 사용한 PWA 조건 충족 확인
