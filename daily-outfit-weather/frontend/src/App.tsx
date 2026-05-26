import { useEffect, useMemo, useState } from 'react'
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
  }
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
    try {
      const currentUser = await request<{ email: string; nickname: string }>('/api/me')
      setUser(currentUser)

      try {
        const nextProfile = await request<ProfileResponse>('/api/profile')
        setProfile(nextProfile)
        setForm(profileToForm(nextProfile))
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
      setHomeKeyword(`${location.sigungu} ${location.dong}`)
      setHomeResults([])
      setHomeSearchState('idle')
    } else {
      setWorkKeyword(`${location.sigungu} ${location.dong}`)
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
          onLocationChange={updateLocation}
          onHomeKeywordChange={setHomeKeyword}
          onWorkKeywordChange={setWorkKeyword}
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
  onLocationChange: (type: 'home' | 'work', location: LocationInput) => void
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
  onLocationChange,
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
          onManualChange={(location) => onLocationChange('home', location)}
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
          onManualChange={(location) => onLocationChange('work', location)}
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
        <button className="primary-button" type="submit" disabled={isBusy}>
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
  onManualChange: (location: LocationInput) => void
}

function LocationPicker({ label, value, keyword, results, searchState, onKeywordChange, onSearch, onSelect, onManualChange }: LocationPickerProps) {
  return (
    <div className="location-picker">
      <label className="field full-width">
        <span>{label} 검색</span>
        <div className="search-line">
          <input
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            placeholder="역삼, 판교"
          />
          <button className="secondary-button" type="button" onClick={() => onSearch(keyword)}>
            검색
          </button>
        </div>
      </label>
      {searchState === 'loading' && <p className="search-note">검색 중입니다.</p>}
      {searchState === 'empty' && <p className="search-note">검색 결과가 없습니다.</p>}
      {results.length > 0 && (
        <div className="search-results">
          {results.map((result) => (
            <button
              type="button"
              key={`${result.sido}-${result.sigungu}-${result.dong}`}
              onClick={() => onSelect(result)}
            >
              <span>{result.sido} {result.sigungu} {result.dong}</span>
              <strong>{result.nx}, {result.ny}</strong>
            </button>
          ))}
        </div>
      )}
      <div className="manual-location-grid">
        <input
          value={value.sido}
          onChange={(event) => onManualChange({ ...value, sido: event.target.value, nx: null, ny: null })}
          placeholder="시도"
          required
        />
        <input
          value={value.sigungu}
          onChange={(event) => onManualChange({ ...value, sigungu: event.target.value, nx: null, ny: null })}
          placeholder="시군구"
          required
        />
        <input
          value={value.dong}
          onChange={(event) => onManualChange({ ...value, dong: event.target.value, nx: null, ny: null })}
          placeholder="동"
          required
        />
      </div>
      {(value.nx || value.ny) && <p className="grid-note">격자 {value.nx}, {value.ny}</p>}
    </div>
  )
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

  return (
    <div className="today-layout">
      <section className="hero-panel">
        <div className="weather-art" aria-hidden="true">
          <span className="sun" />
          <span className="cloud cloud-a" />
          <span className="cloud cloud-b" />
          {recommendation.weatherSummary.rainProbability >= 50 && (
            <>
              <span className="rain-line rain-a" />
              <span className="rain-line rain-b" />
            </>
          )}
        </div>
        <div className="hero-copy">
          <p className="date-label">{todayLabel}</p>
          <h2>{recommendation.summaryMessage}</h2>
          <p>{recommendation.reason}</p>
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

      <section className="weather-strip" aria-label="날씨 요약">
        <Metric label="출근 체감" value={`${recommendation.weatherSummary.commuteFeelsLike}°`} />
        <Metric label="퇴근 체감" value={`${recommendation.weatherSummary.leaveWorkFeelsLike}°`} />
        <Metric label="강수확률" value={`${recommendation.weatherSummary.rainProbability}%`} />
        <Metric label="풍속" value={`${recommendation.weatherSummary.windSpeed}m/s`} />
      </section>

      <section className="recommendation-grid" aria-label="오늘 추천">
        <RecommendationBlock label="상의" value={recommendation.topRecommendation} tone="green" />
        <RecommendationBlock label="외투" value={recommendation.outerRecommendation || '없음'} tone="blue" />
        <RecommendationBlock label="준비물" value={recommendation.itemRecommendation || '가볍게 출발'} tone="amber" />
      </section>

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

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
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
