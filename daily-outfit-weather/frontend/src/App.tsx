import { useEffect, useMemo, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type TransportType = 'WALK' | 'PUBLIC_TRANSPORT' | 'CAR' | 'BICYCLE'
type MessageTone = 'CHARACTER' | 'FRIENDLY' | 'PRACTICAL'
type ChangeAlertOption = 'IMPORTANT_ONLY' | 'ALL' | 'OFF'

type LocationInput = {
  sido: string
  sigungu: string
  dong: string
  nx?: number | null
  ny?: number | null
}

type ProfileForm = {
  nickname: string
  coldSensitivity: number
  heatSensitivity: number
  commuteTime: string
  leaveWorkTime: string
  notificationTime: string
  transportType: TransportType
  messageTone: MessageTone
  changeAlertOption: ChangeAlertOption
  homeLocation: LocationInput
  workLocation: LocationInput
}

type ProfileResponse = ProfileForm & {
  email: string
}

type LocationGrid = LocationInput

type RecommendationResponse = {
  id: number
  targetDate: string
  summaryMessage: string
  characterImageType: string
  topRecommendation: string
  outerRecommendation: string
  itemRecommendation: string
  reason: string
  weatherSummary: {
    commuteFeelsLike: number
    leaveWorkFeelsLike: number
    rainProbability: number
    windSpeed: number
    rainExpected?: boolean
    snowExpected?: boolean
  }
  hourlyForecast?: HourlyForecast[]
}

type HourlyForecast = {
  time: string
  condition: 'SUNNY' | 'CLOUDY' | 'RAIN' | 'SNOW'
  temperature: number
  rainProbability?: number
  commute?: boolean
  leaveWork?: boolean
}

type ViewMode = 'loading' | 'login' | 'onboarding' | 'today' | 'settings'

type FeedbackState = {
  temperature: 'COLD' | 'GOOD' | 'HOT' | null
  rain: 'NEEDED' | 'NOT_NEEDED' | null
}

const emptyLocation: LocationInput = {
  sido: '',
  sigungu: '',
  dong: '',
  nx: null,
  ny: null,
}

const initialForm: ProfileForm = {
  nickname: '',
  coldSensitivity: 3,
  heatSensitivity: 3,
  commuteTime: '08:30',
  leaveWorkTime: '18:30',
  notificationTime: '07:30',
  transportType: 'PUBLIC_TRANSPORT',
  messageTone: 'FRIENDLY',
  changeAlertOption: 'IMPORTANT_ONLY',
  homeLocation: { ...emptyLocation },
  workLocation: { ...emptyLocation },
}

const summerPreviewProfile: ProfileResponse = {
  ...initialForm,
  email: 'preview@daily-outfit-weather.local',
  nickname: '수진',
}

const summerPreviewRecommendation: RecommendationResponse = {
  id: 0,
  targetDate: new Date().toISOString().slice(0, 10),
  summaryMessage: '많이 더워요. 시원한 반팔티로 입어요.',
  characterImageType: 'HOT_LIGHT',
  topRecommendation: '시원한 반팔티',
  outerRecommendation: '통기성 좋은 얇은 하의',
  itemRecommendation: '손선풍기, 물',
  reason: '추천 기준 체감온도는 32도입니다. 많이 더워 땀이 날 수 있으니 수분을 자주 보충하세요.',
  weatherSummary: {
    commuteFeelsLike: 30,
    leaveWorkFeelsLike: 32,
    rainProbability: 10,
    windSpeed: 1.8,
    rainExpected: false,
    snowExpected: false,
  },
  hourlyForecast: [
    { time: '08시', condition: 'SUNNY', temperature: 28, commute: true },
    { time: '10시', condition: 'SUNNY', temperature: 29 },
    { time: '12시', condition: 'SUNNY', temperature: 31 },
    { time: '14시', condition: 'SUNNY', temperature: 33 },
    { time: '16시', condition: 'CLOUDY', temperature: 32 },
    { time: '18시', condition: 'CLOUDY', temperature: 31, leaveWork: true },
    { time: '20시', condition: 'CLOUDY', temperature: 29 },
  ],
}

const transportLabels: Record<TransportType, string> = {
  WALK: '도보',
  PUBLIC_TRANSPORT: '대중교통',
  CAR: '자동차',
  BICYCLE: '자전거',
}

const toneLabels: Record<MessageTone, string> = {
  CHARACTER: '캐릭터',
  FRIENDLY: '부드럽게',
  PRACTICAL: '간결하게',
}

const alertLabels: Record<ChangeAlertOption, string> = {
  IMPORTANT_ONLY: '중요 변화만',
  ALL: '항상',
  OFF: '끄기',
}

function App() {
  const isCharacterPreview = new URLSearchParams(window.location.search).get('preview') === 'characters'
  const [viewMode, setViewMode] = useState<ViewMode>('loading')
  const [user, setUser] = useState<{ email: string; nickname: string } | null>(null)
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [form, setForm] = useState<ProfileForm>(initialForm)
  const [recommendation, setRecommendation] = useState<RecommendationResponse | null>(null)
  const [homeKeyword, setHomeKeyword] = useState('')
  const [workKeyword, setWorkKeyword] = useState('')
  const [homeResults, setHomeResults] = useState<LocationGrid[]>([])
  const [workResults, setWorkResults] = useState<LocationGrid[]>([])
  const [homeSearchState, setHomeSearchState] = useState<'idle' | 'loading' | 'empty'>('idle')
  const [workSearchState, setWorkSearchState] = useState<'idle' | 'loading' | 'empty'>('idle')
  const [feedback, setFeedback] = useState<FeedbackState>({ temperature: null, rain: null })
  const [isFeedbackBusy, setIsFeedbackBusy] = useState(false)
  const [isBusy, setIsBusy] = useState(false)
  const [statusMessage, setStatusMessage] = useState('')

  useEffect(() => {
    void initialize()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const todayLabel = useMemo(() => {
    if (!recommendation?.targetDate) return ''
    return new Intl.DateTimeFormat('ko-KR', {
      month: 'long',
      day: 'numeric',
      weekday: 'long',
    }).format(new Date(`${recommendation.targetDate}T00:00:00`))
  }, [recommendation?.targetDate])

  async function initialize() {
    setViewMode('loading')
    setStatusMessage('')
    if (new URLSearchParams(window.location.search).get('preview') === 'summer') {
      setUser({ email: summerPreviewProfile.email, nickname: summerPreviewProfile.nickname })
      setProfile(summerPreviewProfile)
      setRecommendation(summerPreviewRecommendation)
      setViewMode('today')
      return
    }
    try {
      const currentUser = await request<{ email: string; nickname: string }>('/api/me')
      setUser(currentUser)

      try {
        const nextProfile = await request<ProfileResponse>('/api/profile')
        setProfile(nextProfile)
        setForm(profileToForm(nextProfile))
        setHomeKeyword(locationLabel(nextProfile.homeLocation))
        setWorkKeyword(locationLabel(nextProfile.workLocation))
        setViewMode('today')
        await loadRecommendation()
      } catch (error) {
        if (isApiError(error, 404)) {
          setViewMode('onboarding')
          return
        }
        throw error
      }
    } catch (error) {
      if (isApiError(error, 401)) {
        setViewMode('login')
        return
      }
      setStatusMessage(errorMessage(error))
    }
  }

  const handleLogin = () => {
    window.location.href = '/oauth2/authorization/google'
  }

  const handleLogout = async () => {
    try {
      await request('/api/logout', { method: 'POST' })
      setUser(null)
      setProfile(null)
      setViewMode('login')
    } catch {
      // Even if logout fails, redirect to login
      setUser(null)
      setProfile(null)
      setViewMode('login')
    }
  }

  async function submitFeedback() {
    if (!recommendation || (!feedback.temperature && !feedback.rain)) return

    setIsFeedbackBusy(true)
    try {
      await request(`/api/recommendations/${recommendation.id}/feedback`, {
        method: 'POST',
        body: JSON.stringify({
          temperatureFeedback: feedback.temperature,
          rainFeedback: feedback.rain,
        }),
      })
      setStatusMessage('피드백을 저장했습니다.')
    } catch (error) {
      setStatusMessage(errorMessage(error))
    } finally {
      setIsFeedbackBusy(false)
    }
  }

  async function loadRecommendation() {
    setIsBusy(true)
    try {
      const nextRecommendation = await request<RecommendationResponse>('/api/recommendations/today', {
        method: 'POST',
      })
      setRecommendation(nextRecommendation)
      setFeedback({ temperature: null, rain: null })
      setStatusMessage('')
    } catch (error) {
      setStatusMessage(errorMessage(error))
    } finally {
      setIsBusy(false)
    }
  }

  async function submitProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsBusy(true)
    setStatusMessage('')
    const isSettings = viewMode === 'settings'
    try {
      const nextProfile = await request<ProfileResponse>(isSettings ? '/api/profile' : '/api/profile/onboarding', {
        method: isSettings ? 'PUT' : 'POST',
        body: JSON.stringify(form),
      })
      setProfile(nextProfile)
      setForm(profileToForm(nextProfile))
      setHomeKeyword(locationLabel(nextProfile.homeLocation))
      setWorkKeyword(locationLabel(nextProfile.workLocation))
      setViewMode('today')
      await loadRecommendation()
    } catch (error) {
      setStatusMessage(errorMessage(error))
    } finally {
      setIsBusy(false)
    }
  }

  async function searchLocations(type: 'home' | 'work', keyword: string) {
    const trimmed = keyword.trim()
    if (!trimmed) {
      if (type === 'home') {
        setHomeResults([])
        setHomeSearchState('idle')
      } else {
        setWorkResults([])
        setWorkSearchState('idle')
      }
      return
    }
    try {
      if (type === 'home') {
        setHomeSearchState('loading')
      } else {
        setWorkSearchState('loading')
      }
      const results = await request<LocationGrid[]>(`/api/locations/search?keyword=${encodeURIComponent(trimmed)}`)
      if (type === 'home') {
        setHomeResults(results)
        setHomeSearchState(results.length > 0 ? 'idle' : 'empty')
      } else {
        setWorkResults(results)
        setWorkSearchState(results.length > 0 ? 'idle' : 'empty')
      }
    } catch (error) {
      if (type === 'home') {
        setHomeResults([])
        setHomeSearchState('empty')
      } else {
        setWorkResults([])
        setWorkSearchState('empty')
      }
      setStatusMessage(errorMessage(error))
    }
  }

  function selectLocation(type: 'home' | 'work', location: LocationGrid) {
    updateLocation(type, location)
    if (type === 'home') {
      setHomeKeyword(locationLabel(location))
      setHomeResults([])
      setHomeSearchState('idle')
    } else {
      setWorkKeyword(locationLabel(location))
      setWorkResults([])
      setWorkSearchState('idle')
    }
  }

  function updateLocationKeyword(type: 'home' | 'work', keyword: string) {
    updateLocation(type, { ...emptyLocation })
    if (type === 'home') {
      setHomeKeyword(keyword)
      setHomeResults([])
      setHomeSearchState('idle')
    } else {
      setWorkKeyword(keyword)
      setWorkResults([])
      setWorkSearchState('idle')
    }
  }

  function updateLocation(type: 'home' | 'work', location: LocationInput) {
    setForm((current) => ({
      ...current,
      [type === 'home' ? 'homeLocation' : 'workLocation']: location,
    }))
  }

  function updateField<K extends keyof ProfileForm>(field: K, value: ProfileForm[K]) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function renderContent() {
    if (viewMode === 'loading') {
      return <LoadingPanel onRetry={initialize} />
    }

    if (viewMode === 'login') {
      return (
        <section className="login-panel">
          <div className="login-visual" aria-hidden="true" />
          <h2>반가워요!</h2>
          <p>오늘 날씨에 딱 맞는 옷차림을 추천해드릴게요.</p>
          <button className="primary-button google-login" type="button" onClick={handleLogin}>
            Google로 시작하기
          </button>
        </section>
      )
    }

    if (viewMode === 'onboarding' || viewMode === 'settings') {
      return (
        <ProfileEditor
          form={form}
          mode={viewMode}
          isBusy={isBusy}
          homeKeyword={homeKeyword}
          workKeyword={workKeyword}
          homeResults={homeResults}
          workResults={workResults}
          homeSearchState={homeSearchState}
          workSearchState={workSearchState}
          onFieldChange={updateField}
          onHomeKeywordChange={(keyword) => updateLocationKeyword('home', keyword)}
          onWorkKeywordChange={(keyword) => updateLocationKeyword('work', keyword)}
          onSearch={searchLocations}
          onSelectLocation={selectLocation}
          onSubmit={submitProfile}
          onCancel={() => setViewMode('today')}
        />
      )
    }

    return (
      <TodayDashboard
        profile={profile}
        recommendation={recommendation}
        todayLabel={todayLabel}
        feedback={feedback}
        isBusy={isBusy}
        onRefresh={loadRecommendation}
        onSettings={() => setViewMode('settings')}
        onFeedbackChange={setFeedback}
        onFeedbackSubmit={submitFeedback}
        isFeedbackBusy={isFeedbackBusy}
      />
    )
  }

  if (isCharacterPreview) {
    return <CharacterPreview />
  }

  return (
    <main className="app-shell">
      <div className="app-frame">
        <header className="topbar">
          <div>
            <p className="eyebrow">Daily Outfit Weather</p>
            <h1>오늘 뭐입지</h1>
          </div>
          <div className="topbar-actions">
            {user && (
              <div className="user-info">
                <span className="profile-chip">{profile?.nickname || user.nickname}</span>
                <button className="text-button" type="button" onClick={handleLogout}>
                  로그아웃
                </button>
              </div>
            )}
            {viewMode === 'today' && (
              <button className="icon-button" type="button" onClick={() => setViewMode('settings')} aria-label="설정">
                설정
              </button>
            )}
          </div>
        </header>
        {statusMessage && <p className="status-message">{statusMessage}</p>}
        {renderContent()}
      </div>
    </main>
  )
}

type CharacterPreviewScenario = {
  label: string
  description: string
  imageType: string
  rain?: boolean
  snow?: boolean
  cloudy?: boolean
  windy?: boolean
  dust?: boolean
  theme?: 'default' | 'hot' | 'rain' | 'cold' | 'wind' | 'dust'
}

const characterPreviewScenarios: CharacterPreviewScenario[] = [
  { label: '많이 더운 날', description: '지친 표정 · 붉은 볼 · 땀방울', imageType: 'HOT_LIGHT', theme: 'hot' },
  { label: '따뜻한 날', description: '반팔티 · 모자', imageType: 'WARM_LIGHT', theme: 'hot' },
  { label: '선선한 날', description: '가벼운 셔츠', imageType: 'MILD_LONG_SLEEVE' },
  { label: '비 오는 날', description: '우비 · 우산 · 빗줄기', imageType: 'MILD_LONG_SLEEVE', rain: true, theme: 'rain' },
  { label: '흐린 날', description: '해 없이 구름 낀 하늘', imageType: 'MILD_LONG_SLEEVE', cloudy: true, theme: 'rain' },
  { label: '눈 오는 날', description: '패딩 · 목도리 · 눈송이', imageType: 'VERY_COLD_PADDING', snow: true, theme: 'cold' },
  { label: '바람 부는 날', description: '바람막이 · 바람선', imageType: 'WINDY_LIGHT_OUTER', windy: true, theme: 'wind' },
  { label: '미세먼지 있는 날', description: '마스크 · 마스크 쓴 작은 강아지', imageType: 'DUST_MASK', dust: true, theme: 'dust' },
  { label: '추운 날', description: '패딩 · 목도리', imageType: 'VERY_COLD_PADDING', theme: 'cold' },
]

function CharacterPreview() {
  const [isNightPreview, setIsNightPreview] = useState(false)

  return (
    <main className="app-shell character-preview-shell">
      <div className="app-frame">
        <header className="topbar">
          <div>
            <p className="eyebrow">Weatherwear Preview</p>
            <h1>날씨별 캐릭터 미리보기</h1>
          </div>
          <div className="preview-actions">
            <button
              className={`preview-toggle ${isNightPreview ? 'active' : ''}`}
              type="button"
              onClick={() => setIsNightPreview((current) => !current)}
            >
              {isNightPreview ? '낮 배경 보기' : '밤 배경 보기'}
            </button>
            <a className="preview-link" href="/?preview=summer">여름 메인 화면 보기</a>
          </div>
        </header>
        <p className="preview-description">메인 화면에서 사용할 캐릭터와 배경 효과를 한 번에 비교합니다.</p>
        <section className="character-preview-grid" aria-label="날씨별 메인 캐릭터 미리보기">
          {characterPreviewScenarios.map((scenario) => (
            <article className="character-preview-card" key={scenario.label}>
              <div className={`weather-art preview-weather-art theme-${scenario.theme ?? 'default'} ${scenario.snow || scenario.dust ? 'with-sidekick' : ''} ${isNightPreview ? 'is-night' : ''}`}>
                {!scenario.cloudy && !scenario.rain && !scenario.snow && !isNightPreview && <span className="sun" />}
                {isNightPreview && <NightSky />}
                <span className="cloud cloud-a" />
                <span className="cloud cloud-b" />
                {scenario.rain && <Rainfall />}
                {scenario.windy && (
                  <>
                    <span className="wind-line wind-a" />
                    <span className="wind-line wind-b" />
                    <span className="wind-line wind-c" />
                  </>
                )}
                {scenario.snow && (
                  <>
                    <Snowfall />
                    <Snowman />
                  </>
                )}
                {scenario.dust && <MaskedDog />}
                <OutfitCharacter imageType={scenario.imageType} hasUmbrella={scenario.rain ?? false} />
              </div>
              <div>
                <h2>{scenario.label}</h2>
                <p>{scenario.description}</p>
              </div>
            </article>
          ))}
        </section>
      </div>
    </main>
  )
}

function LoadingPanel({ onRetry }: { onRetry: () => void }) {
  return (
    <section className="loading-panel" aria-live="polite">
      <div className="loader" />
      <p>오늘 추천을 준비하고 있습니다.</p>
      <button className="secondary-button" type="button" onClick={onRetry}>
        다시 시도
      </button>
    </section>
  )
}

type ProfileEditorProps = {
  form: ProfileForm
  mode: 'onboarding' | 'settings'
  isBusy: boolean
  homeKeyword: string
  workKeyword: string
  homeResults: LocationGrid[]
  workResults: LocationGrid[]
  homeSearchState: 'idle' | 'loading' | 'empty'
  workSearchState: 'idle' | 'loading' | 'empty'
  onFieldChange: <K extends keyof ProfileForm>(field: K, value: ProfileForm[K]) => void
  onHomeKeywordChange: (value: string) => void
  onWorkKeywordChange: (value: string) => void
  onSearch: (type: 'home' | 'work', keyword: string) => void
  onSelectLocation: (type: 'home' | 'work', location: LocationGrid) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onCancel: () => void
}

function ProfileEditor({
  form,
  mode,
  isBusy,
  homeKeyword,
  workKeyword,
  homeResults,
  workResults,
  homeSearchState,
  workSearchState,
  onFieldChange,
  onHomeKeywordChange,
  onWorkKeywordChange,
  onSearch,
  onSelectLocation,
  onSubmit,
  onCancel,
}: ProfileEditorProps) {
  return (
    <form className="profile-layout" onSubmit={onSubmit}>
      <section className="form-section identity-section">
        <div className="section-heading">
          <span>01</span>
          <h2>{mode === 'onboarding' ? '기본 설정' : '설정 변경'}</h2>
        </div>
        <label className="field full-width">
          <span>닉네임</span>
          <input
            value={form.nickname}
            maxLength={50}
            onChange={(event) => onFieldChange('nickname', event.target.value)}
            placeholder="수진"
            required
          />
        </label>
        <div className="field-grid">
          <label className="field">
            <span>출근</span>
            <input
              type="time"
              value={form.commuteTime}
              onChange={(event) => onFieldChange('commuteTime', event.target.value)}
              required
            />
          </label>
          <label className="field">
            <span>퇴근</span>
            <input
              type="time"
              value={form.leaveWorkTime}
              onChange={(event) => onFieldChange('leaveWorkTime', event.target.value)}
              required
            />
          </label>
          <label className="field">
            <span>알림</span>
            <input
              type="time"
              value={form.notificationTime}
              onChange={(event) => onFieldChange('notificationTime', event.target.value)}
              required
            />
          </label>
        </div>
      </section>

      <section className="form-section location-section">
        <div className="section-heading">
          <span>02</span>
          <h2>위치</h2>
        </div>
        <LocationPicker
          label="집"
          value={form.homeLocation}
          keyword={homeKeyword}
          results={homeResults}
          searchState={homeSearchState}
          onKeywordChange={onHomeKeywordChange}
          onSearch={(keyword) => onSearch('home', keyword)}
          onSelect={(location) => onSelectLocation('home', location)}
        />
        <LocationPicker
          label="직장"
          value={form.workLocation}
          keyword={workKeyword}
          results={workResults}
          searchState={workSearchState}
          onKeywordChange={onWorkKeywordChange}
          onSearch={(keyword) => onSearch('work', keyword)}
          onSelect={(location) => onSelectLocation('work', location)}
        />
      </section>

      <section className="form-section preference-section">
        <div className="section-heading">
          <span>03</span>
          <h2>취향</h2>
        </div>
        <div className="slider-row">
          <label>
            <span>추위 민감도</span>
            <strong>{form.coldSensitivity}</strong>
          </label>
          <input
            type="range"
            min="1"
            max="5"
            value={form.coldSensitivity}
            onChange={(event) => onFieldChange('coldSensitivity', Number(event.target.value))}
          />
        </div>
        <div className="slider-row">
          <label>
            <span>더위 민감도</span>
            <strong>{form.heatSensitivity}</strong>
          </label>
          <input
            type="range"
            min="1"
            max="5"
            value={form.heatSensitivity}
            onChange={(event) => onFieldChange('heatSensitivity', Number(event.target.value))}
          />
        </div>
        <SegmentedControl
          label="이동수단"
          value={form.transportType}
          options={transportLabels}
          onChange={(value) => onFieldChange('transportType', value)}
        />
        <SegmentedControl
          label="말투"
          value={form.messageTone}
          options={toneLabels}
          onChange={(value) => onFieldChange('messageTone', value)}
        />
        <SegmentedControl
          label="변경 알림"
          value={form.changeAlertOption}
          options={alertLabels}
          onChange={(value) => onFieldChange('changeAlertOption', value)}
        />
      </section>

      <div className="form-actions">
        {mode === 'settings' && (
          <button className="secondary-button" type="button" onClick={onCancel}>
            취소
          </button>
        )}
        <button className="primary-button" type="submit" disabled={isBusy || !isLocationSelected(form.homeLocation) || !isLocationSelected(form.workLocation)}>
          {isBusy ? '저장 중' : mode === 'onboarding' ? '추천 시작' : '변경 저장'}
        </button>
      </div>
    </form>
  )
}

type LocationPickerProps = {
  label: string
  value: LocationInput
  keyword: string
  results: LocationGrid[]
  searchState: 'idle' | 'loading' | 'empty'
  onKeywordChange: (value: string) => void
  onSearch: (keyword: string) => void
  onSelect: (location: LocationGrid) => void
}

function LocationPicker({ label, value, keyword, results, searchState, onKeywordChange, onSearch, onSelect }: LocationPickerProps) {
  const onSearchRef = useRef(onSearch)

  useEffect(() => {
    onSearchRef.current = onSearch
  }, [onSearch])

  useEffect(() => {
    const trimmed = keyword.trim()
    if (trimmed.length < 2 || (isLocationSelected(value) && trimmed === locationLabel(value))) return

    const timeoutId = window.setTimeout(() => onSearchRef.current(trimmed), 250)
    return () => window.clearTimeout(timeoutId)
  }, [keyword, value])

  return (
    <div className="location-picker">
      <label className="field full-width">
        <span>{label} 동네</span>
        <input
          value={keyword}
          onChange={(event) => onKeywordChange(event.target.value)}
          placeholder="동 이름을 입력하세요. 예: 성내동, 잠실동"
          autoComplete="off"
        />
      </label>
      {keyword.trim().length === 1 && <p className="search-note">동 이름을 2글자 이상 입력해 주세요.</p>}
      {searchState === 'loading' && <p className="search-note">검색 중입니다.</p>}
      {searchState === 'empty' && <p className="search-note">검색 결과가 없습니다. 동 이름을 확인해 주세요.</p>}
      {results.length > 0 && (
        <div className="search-results">
          {results.slice(0, 8).map((result) => (
            <button
              type="button"
              key={`${result.sido}-${result.sigungu}-${result.dong}`}
              onClick={() => onSelect(result)}
            >
              <span>{result.sido} {result.sigungu} {result.dong}</span>
              <strong>선택</strong>
            </button>
          ))}
        </div>
      )}
      {isLocationSelected(value) && <p className="selected-location">선택됨: {locationLabel(value)}</p>}
    </div>
  )
}

function isLocationSelected(location: LocationInput) {
  return location.nx != null && location.ny != null
}

function locationLabel(location: LocationInput) {
  return `${location.sido} ${location.sigungu} ${location.dong}`.trim()
}

type SegmentedControlProps<T extends string> = {
  label: string
  value: T
  options: Record<T, string>
  onChange: (value: T) => void
}

function SegmentedControl<T extends string>({ label, value, options, onChange }: SegmentedControlProps<T>) {
  return (
    <fieldset className="segmented-field">
      <legend>{label}</legend>
      <div className="segmented-control">
        {(Object.keys(options) as T[]).map((option) => (
          <button
            key={option}
            type="button"
            className={option === value ? 'active' : ''}
            onClick={() => onChange(option)}
          >
            {options[option]}
          </button>
        ))}
      </div>
    </fieldset>
  )
}

type TodayDashboardProps = {
  profile: ProfileResponse | null
  recommendation: RecommendationResponse | null
  todayLabel: string
  feedback: FeedbackState
  isBusy: boolean
  onRefresh: () => void
  onSettings: () => void
  onFeedbackChange: (feedback: FeedbackState) => void
  onFeedbackSubmit: () => void
  isFeedbackBusy: boolean
}

function TodayDashboard({
  profile,
  recommendation,
  todayLabel,
  feedback,
  isBusy,
  onRefresh,
  onSettings,
  onFeedbackChange,
  onFeedbackSubmit,
  isFeedbackBusy,
}: TodayDashboardProps) {
  if (!recommendation) {
    return (
      <section className="empty-state">
        <p>오늘 추천을 아직 불러오지 못했습니다.</p>
        <button className="primary-button" type="button" onClick={onRefresh} disabled={isBusy}>
          다시 불러오기
        </button>
      </section>
    )
  }

  const isWindy = recommendation.weatherSummary.windSpeed >= 4
  const isSnowy = recommendation.weatherSummary.snowExpected === true
  const isRainy = !isSnowy && (recommendation.weatherSummary.rainExpected
    ?? recommendation.weatherSummary.rainProbability >= 60)
  const isCloudy = isRainy || isSnowy
  const isNight = isNightInSeoul()

  return (
    <div className="today-layout">
      <section className="hero-panel">
        <div className="hero-copy">
          <p className="date-label">{todayLabel}</p>
          <p className="outfit-label">오늘의 웨더웨어</p>
          <h2>{recommendation.summaryMessage}</h2>
          <p>{recommendation.reason}</p>
        </div>
        <div className={`weather-art ${isNight ? 'is-night' : ''}`} aria-hidden="true">
          {!isCloudy && !isNight && <span className="sun" />}
          {isNight && <NightSky />}
          <span className="cloud cloud-a" />
          <span className="cloud cloud-b" />
          {isRainy && <Rainfall />}
          {isSnowy && (
            <>
              <Snowfall />
              <Snowman />
            </>
          )}
          {isWindy && (
            <>
              <span className="wind-line wind-a" />
              <span className="wind-line wind-b" />
              <span className="wind-line wind-c" />
            </>
          )}
          <OutfitCharacter
            imageType={recommendation.characterImageType}
            hasUmbrella={isRainy}
          />
        </div>
        <div className="hero-outfit-summary">
          <span>오늘 이렇게 입어요</span>
          <strong>
            {[recommendation.topRecommendation, recommendation.outerRecommendation].filter(Boolean).join(' + ')}
          </strong>
          <small>{recommendation.itemRecommendation || '추가 준비물 없이 가볍게 출발하세요.'}</small>
        </div>
        <div className="hero-actions">
          <button className="secondary-button" type="button" onClick={onRefresh} disabled={isBusy}>
            새로고침
          </button>
          <button className="secondary-button" type="button" onClick={onSettings}>
            설정 변경
          </button>
        </div>
      </section>

      <section className="scroll-guide" aria-label="상세 정보 안내">
        <span>아래로 내려서 자세히 보기</span>
        <strong>↓</strong>
      </section>

      <section className="section-intro">
        <p className="panel-kicker">오늘 날씨 해석</p>
        <h3>숫자보다 먼저, 외출할 때 느껴질 날씨예요.</h3>
      </section>

      <section className="weather-strip" aria-label="날씨 요약">
        <Metric
          label="출근길"
          value={`${recommendation.weatherSummary.commuteFeelsLike}°`}
          description={temperatureDescription(recommendation.weatherSummary.commuteFeelsLike)}
          detailLabel="체감"
        />
        <Metric
          label="퇴근길"
          value={`${recommendation.weatherSummary.leaveWorkFeelsLike}°`}
          description={temperatureDescription(recommendation.weatherSummary.leaveWorkFeelsLike)}
          detailLabel="체감"
        />
        <Metric
          label="비 소식"
          value={`${recommendation.weatherSummary.rainProbability}%`}
          description={rainDescription(recommendation.weatherSummary.rainProbability)}
          detailLabel="강수확률"
        />
        <Metric
          label="바람"
          value={`${recommendation.weatherSummary.windSpeed}m/s`}
          description={windDescription(recommendation.weatherSummary.windSpeed)}
          detailLabel="풍속"
        />
      </section>

      {recommendation.hourlyForecast && recommendation.hourlyForecast.length > 0 && (
        <HourlyWeather forecast={recommendation.hourlyForecast} />
      )}

      <section className="section-intro">
        <p className="panel-kicker">준비 완료</p>
        <h3>옷장에서 챙길 것만 빠르게 확인하세요.</h3>
      </section>

      <section className="recommendation-grid" aria-label="오늘 추천">
        <RecommendationBlock label="상의" value={recommendation.topRecommendation} tone="green" />
        <RecommendationBlock label="외투" value={recommendation.outerRecommendation || '없음'} tone="blue" />
        <RecommendationBlock label="준비물" value={recommendation.itemRecommendation || '가볍게 출발'} tone="amber" />
      </section>

      <section className="sun-care-panel" aria-label="자외선 대비 안내">
        <div>
          <p className="panel-kicker">햇빛 대비</p>
          <h3>자외선이 높은 날에는 햇빛 준비물도 알려드릴게요.</h3>
          <p>별도 자외선 API 연동 후, 필요한 날에만 최대 3개를 우선순위로 추천합니다.</p>
        </div>
        <div className="sun-care-items" aria-label="자외선 대비 준비물">
          <span>선크림</span>
          <span>선글라스</span>
          <span>양산</span>
        </div>
      </section>

      <section className="section-intro">
        <p className="panel-kicker">웨더웨어 캐릭터</p>
        <h3>날씨에 따라 이렇게 준비해요.</h3>
      </section>

      <MascotGallery />

      <section className="feedback-panel">
        <div>
          <p className="panel-kicker">피드백</p>
          <h3>{profile?.nickname ? `${profile.nickname}님에게 맞았나요?` : '오늘 추천은 어땠나요?'}</h3>
        </div>
        <div className="feedback-groups">
          <div className="feedback-buttons" aria-label="체감 피드백">
            <button
              type="button"
              className={feedback.temperature === 'COLD' ? 'active' : ''}
              onClick={() => onFeedbackChange({ ...feedback, temperature: 'COLD' })}
            >
              추웠어요
            </button>
            <button
              type="button"
              className={feedback.temperature === 'GOOD' ? 'active' : ''}
              onClick={() => onFeedbackChange({ ...feedback, temperature: 'GOOD' })}
            >
              딱 좋아요
            </button>
            <button
              type="button"
              className={feedback.temperature === 'HOT' ? 'active' : ''}
              onClick={() => onFeedbackChange({ ...feedback, temperature: 'HOT' })}
            >
              더웠어요
            </button>
          </div>
          <div className="feedback-buttons" aria-label="우산 피드백">
            <button
              type="button"
              className={feedback.rain === 'NEEDED' ? 'active' : ''}
              onClick={() => onFeedbackChange({ ...feedback, rain: 'NEEDED' })}
            >
              우산 필요
            </button>
            <button
              type="button"
              className={feedback.rain === 'NOT_NEEDED' ? 'active' : ''}
              onClick={() => onFeedbackChange({ ...feedback, rain: 'NOT_NEEDED' })}
            >
              우산 적절
            </button>
          </div>
          <button
            className="primary-button feedback-submit"
            type="button"
            onClick={onFeedbackSubmit}
            disabled={isFeedbackBusy || (!feedback.temperature && !feedback.rain)}
          >
            저장
          </button>
        </div>
      </section>
    </div>
  )
}

function isNightInSeoul() {
  const hour = Number(new Intl.DateTimeFormat('en-US', {
    hour: '2-digit',
    hour12: false,
    timeZone: 'Asia/Seoul',
  }).format(new Date()))
  return hour < 6 || hour >= 18
}

function NightSky() {
  return (
    <div className="night-sky" aria-hidden="true">
      <span className="moon" />
      <span className="star star-a">✦</span>
      <span className="star star-b">✧</span>
      <span className="star star-c">✦</span>
      <span className="star star-d">✧</span>
    </div>
  )
}

function Rainfall() {
  return (
    <div className="rainfall" aria-hidden="true">
      {['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'].map((raindrop) => (
        <span className={`rain-line rain-${raindrop}`} key={raindrop} />
      ))}
    </div>
  )
}

function Snowfall() {
  return (
    <div className="snowfall" aria-hidden="true">
      {['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'].map((snowflake) => (
        <span className={`snowflake snow-${snowflake}`} key={snowflake}>✦</span>
      ))}
    </div>
  )
}

function Snowman() {
  return <img className="snowman" src="/mascot/snowman.svg?v=1" alt="" aria-hidden="true" />
}

function MaskedDog() {
  return <img className="masked-dog" src="/mascot/masked-dog.svg?v=1" alt="" aria-hidden="true" />
}

const hourlyConditionLabels: Record<HourlyForecast['condition'], string> = {
  SUNNY: '맑음',
  CLOUDY: '구름',
  RAIN: '비',
  SNOW: '눈',
}

const hourlyConditionIcons: Record<HourlyForecast['condition'], string> = {
  SUNNY: '☀',
  CLOUDY: '☁',
  RAIN: '☂',
  SNOW: '✦',
}

function HourlyWeather({ forecast }: { forecast: HourlyForecast[] }) {
  return (
    <section className="hourly-panel" aria-label="시간별 날씨">
      <div className="hourly-heading">
        <div>
          <p className="panel-kicker">시간별 날씨</p>
          <h3>외출하는 동안 날씨 흐름을 확인하세요.</h3>
        </div>
        <span>옆으로 밀어보기 →</span>
      </div>
      <div className="hourly-scroll">
        {forecast.map((hour) => (
          <article className="hourly-card" key={hour.time}>
            <strong>{hour.time}</strong>
            <span className={`hourly-icon ${hour.condition.toLowerCase()}`} aria-hidden="true">
              {hourlyConditionIcons[hour.condition]}
            </span>
            <span>{hourlyConditionLabels[hour.condition]}</span>
            <b>{hour.temperature}°</b>
            {(hour.condition === 'RAIN' || hour.condition === 'SNOW') && hour.rainProbability != null && (
              <small>비 {hour.rainProbability}%</small>
            )}
            {hour.commute && <em>출근</em>}
            {hour.leaveWork && <em>퇴근</em>}
          </article>
        ))}
      </div>
    </section>
  )
}

const mascotScenarios = [
  { image: '/mascot/mild.png', label: '포근한 날', description: '가벼운 셔츠' },
  { image: '/mascot/hot.png', label: '더운 날', description: '반팔과 모자' },
  { image: '/mascot/rain.png', label: '비 오는 날', description: '우비와 우산' },
  { image: '/mascot/snow.png', label: '추운 날', description: '패딩과 목도리' },
  { image: '/mascot/wind.png', label: '바람 부는 날', description: '가벼운 바람막이' },
  { image: '/mascot/hot.png', label: '자외선 높은 날', description: '선크림과 선글라스' },
  { image: '/mascot/dust.png', label: '황사 있는 날', description: '마스크 챙기기' },
]

function MascotGallery() {
  return (
    <section className="mascot-gallery" aria-label="날씨별 캐릭터">
      {mascotScenarios.map((scenario) => (
        <article className="mascot-card" key={scenario.label}>
          <div className="mascot-card-art">
            <img src={scenario.image} alt="" />
          </div>
          <span>{scenario.label}</span>
          <strong>{scenario.description}</strong>
        </article>
      ))}
    </section>
  )
}

function OutfitCharacter({
  imageType,
  hasUmbrella,
}: {
  imageType: string
  hasUmbrella: boolean
}) {
  return (
    <div className={`outfit-character ${imageType.toLowerCase()}`}>
      <img src={mascotImage(imageType, hasUmbrella)} alt="" />
      {!hasUmbrella && imageType === 'HOT_LIGHT' && (
        <img className="character-expression" src="/mascot/hot-expression.svg?v=3" alt="" />
      )}
      {imageType === 'DUST_MASK' && (
        <img className="character-expression" src="/mascot/dust-mask-expression.svg?v=1" alt="" />
      )}
    </div>
  )
}

function mascotImage(imageType: string, hasUmbrella: boolean) {
  if (hasUmbrella) return '/mascot/rain.png'

  const imageByType: Record<string, string> = {
    HOT_LIGHT: '/mascot/hot.png',
    WARM_LIGHT: '/mascot/hot.png',
    MILD_LONG_SLEEVE: '/mascot/mild.png',
    COOL_CARDIGAN: '/mascot/mild.png',
    WINDY_LIGHT_OUTER: '/mascot/wind.png',
    COLD_COAT: '/mascot/snow.png',
    VERY_COLD_PADDING: '/mascot/snow.png',
    FREEZING_PADDING: '/mascot/snow.png',
    DUST_MASK: '/mascot/dust.png',
  }
  return imageByType[imageType] ?? imageByType.WARM_LIGHT
}

function Metric({ label, value, description, detailLabel }: { label: string; value: string; description: string; detailLabel: string }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{description}</strong>
      <p>{detailLabel} {value}</p>
    </div>
  )
}

function RecommendationBlock({ label, value, tone }: { label: string; value: string; tone: 'green' | 'blue' | 'amber' }) {
  return (
    <div className={`recommendation-block ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}

function normalizeTime(value: string) {
  return value.length >= 5 ? value.slice(0, 5) : value
}

function temperatureDescription(temperature: number) {
  if (temperature >= 28) return '더위를 느끼기 쉬워요'
  if (temperature >= 23) return '가볍게 입기 좋아요'
  if (temperature >= 20) return '얇은 긴팔이 편해요'
  if (temperature >= 17) return '가디건이 있으면 좋아요'
  if (temperature >= 12) return '가벼운 외투가 필요해요'
  if (temperature >= 8) return '코트를 챙겨 입으세요'
  return '따뜻하게 입으세요'
}

function rainDescription(probability: number) {
  if (probability >= 70) return '우산을 꼭 챙기세요'
  if (probability >= 50) return '작은 우산이 있으면 좋아요'
  if (probability >= 30) return '비 소식을 확인해 주세요'
  return '우산은 필요 없어요'
}

function windDescription(speed: number) {
  if (speed >= 9) return '바람이 매우 강해요'
  if (speed >= 5) return '바람이 꽤 불어요'
  if (speed >= 2) return '선선한 바람이 불어요'
  return '바람은 거의 없어요'
}

function profileToForm(profile: ProfileResponse): ProfileForm {
  return {
    nickname: profile.nickname,
    coldSensitivity: profile.coldSensitivity,
    heatSensitivity: profile.heatSensitivity,
    commuteTime: normalizeTime(profile.commuteTime),
    leaveWorkTime: normalizeTime(profile.leaveWorkTime),
    notificationTime: normalizeTime(profile.notificationTime),
    transportType: profile.transportType,
    messageTone: profile.messageTone,
    changeAlertOption: profile.changeAlertOption,
    homeLocation: profile.homeLocation,
    workLocation: profile.workLocation,
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new ApiError(response.status, body?.message ?? '요청을 처리하지 못했습니다.')
  }

  return response.json() as Promise<T>
}

class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

function isApiError(error: unknown, status: number) {
  return error instanceof ApiError && error.status === status
}

function errorMessage(error: unknown) {
  if (error instanceof Error) return error.message
  return '알 수 없는 오류가 발생했습니다.'
}

export default App
