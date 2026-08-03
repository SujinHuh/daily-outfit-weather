package com.dailyoutfitweather.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;
import com.dailyoutfitweather.recommendation.domain.RecommendationWeatherSnapshot;
import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.RecommendationInput;
import com.dailyoutfitweather.recommendation.engine.WeeklyOutfitEngine;
import com.dailyoutfitweather.recommendation.dto.RecommendationResponse;
import com.dailyoutfitweather.recommendation.dto.RecommendationResult;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.recommendation.engine.RuleBasedRecommendationEngine;
import com.dailyoutfitweather.recommendation.repository.OutfitRecommendationRepository;
import com.dailyoutfitweather.recommendation.service.RecommendationService;
import com.dailyoutfitweather.recommendation.service.WeatherSnapshotProvider;
import com.dailyoutfitweather.recommendation.service.WeatherSnapshotProvider.WeatherSnapshots;
import com.dailyoutfitweather.user.domain.AuthProvider;
import com.dailyoutfitweather.user.domain.ChangeAlertOption;
import com.dailyoutfitweather.user.domain.MessageTone;
import com.dailyoutfitweather.user.domain.TransportType;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.UserProfileRepository;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

	private static final String DEV_USER_EMAIL = "dev-user@daily-outfit-weather.local";
	private static final LocalDate TARGET_DATE = LocalDate.of(2026, 5, 25);

	@Mock
	private UserProfileRepository userProfileRepository;

	@Mock
	private OutfitRecommendationRepository outfitRecommendationRepository;

	@Mock
	private WeatherSnapshotProvider weatherSnapshotProvider;

	@Mock
	private RuleBasedRecommendationEngine recommendationEngine;

	private final WeeklyOutfitEngine weeklyOutfitEngine = new WeeklyOutfitEngine();

	private final Clock clock = Clock.fixed(Instant.parse("2026-05-24T15:30:00Z"), ZoneId.of("Asia/Seoul"));

	private RecommendationService recommendationService;

	@BeforeEach
	void setUp() {
		recommendationService = new RecommendationService(
			userProfileRepository,
			outfitRecommendationRepository,
			weatherSnapshotProvider,
			recommendationEngine,
			weeklyOutfitEngine,
			clock
		);
	}

	@Test
	void returnsExistingRecommendationForFixedSeoulDateWithoutRecreating() {
		User user = devUser();
		UserProfile profile = profile(user);
		OutfitRecommendation existing = recommendation(user, TARGET_DATE);
		when(userProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
		when(outfitRecommendationRepository.findByUserIdAndTargetDate(user.getId(), TARGET_DATE))
			.thenReturn(Optional.of(existing));

		RecommendationResponse response = recommendationService.getOrCreateTodayRecommendation(user);

		assertThat(response.targetDate()).isEqualTo(TARGET_DATE);
		verify(weatherSnapshotProvider, never()).getTodayWeather(user, profile);
		verify(recommendationEngine, never()).recommend(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void refreshesExistingRecommendationWhenStoredHumidityIsMissing() {
		User user = devUser();
		UserProfile profile = profile(user);
		OutfitRecommendation existing = staleRecommendation(user, TARGET_DATE);
		WeatherSnapshot commuteWeather = new WeatherSnapshot(30, 32, 20, PrecipitationType.NONE, 2.4, 79);
		WeatherSnapshot leaveWorkWeather = new WeatherSnapshot(31, 34, 30, PrecipitationType.NONE, 2.8, 82);
		RecommendationResult result = new RecommendationResult(
			34,
			"반팔티",
			"없음",
			"물",
			java.util.List.of("물"),
			"후텁지근하게 더워요. 시원하게 입고 물을 챙겨요.",
			"습도 82%로 후텁지근함을 반영했습니다.",
			"HOT_SUMMER"
		);
		when(userProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
		when(outfitRecommendationRepository.findByUserIdAndTargetDate(user.getId(), TARGET_DATE))
			.thenReturn(Optional.of(existing));
		when(weatherSnapshotProvider.getTodayWeather(user, profile))
			.thenReturn(new WeatherSnapshots(commuteWeather, leaveWorkWeather));
		when(recommendationEngine.recommend(org.mockito.ArgumentMatchers.any(RecommendationInput.class))).thenReturn(result);

		RecommendationResponse response = recommendationService.getOrCreateTodayRecommendation(user);

		assertThat(response.weatherSummary().humidity()).isEqualTo(82);
		assertThat(response.summaryMessage()).contains("후텁지근");
		assertThat(existing.getWeatherSnapshot().weatherSummary().humidity()).isEqualTo(82);
		verify(outfitRecommendationRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void createsRecommendationForFixedSeoulDateAndStoresFullWeatherSnapshot() {
		User user = devUser();
		UserProfile profile = profile(user);
		WeatherSnapshot commuteWeather = new WeatherSnapshot(17, 16, 20, PrecipitationType.NONE, 2.4);
		WeatherSnapshot leaveWorkWeather = new WeatherSnapshot(14, 13, 30, PrecipitationType.RAIN, 2.8);
		RecommendationResult result = result();
		when(userProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
		when(outfitRecommendationRepository.findByUserIdAndTargetDate(user.getId(), TARGET_DATE))
			.thenReturn(Optional.empty());
		when(weatherSnapshotProvider.getTodayWeather(user, profile))
			.thenReturn(new WeatherSnapshots(commuteWeather, leaveWorkWeather));
		when(recommendationEngine.recommend(org.mockito.ArgumentMatchers.any(RecommendationInput.class))).thenReturn(result);
		when(outfitRecommendationRepository.save(org.mockito.ArgumentMatchers.any(OutfitRecommendation.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		RecommendationResponse response = recommendationService.getOrCreateTodayRecommendation(user);

		assertThat(response.targetDate()).isEqualTo(TARGET_DATE);
		assertThat(response.weatherSummary().leaveWorkFeelsLike()).isEqualTo(13);
		ArgumentCaptor<OutfitRecommendation> captor = ArgumentCaptor.forClass(OutfitRecommendation.class);
		verify(outfitRecommendationRepository).save(captor.capture());
		RecommendationWeatherSnapshot weatherSnapshot = captor.getValue().getWeatherSnapshot();
		assertThat(weatherSnapshot.commuteWeather()).isEqualTo(commuteWeather);
		assertThat(weatherSnapshot.leaveWorkWeather()).isEqualTo(leaveWorkWeather);
		assertThat(weatherSnapshot.weatherSummary().rainProbability()).isEqualTo(30);
	}

	@Test
	void returns7DayWeeklyRecommendationForUser() {
		User user = devUser();
		UserProfile profile = profile(user);
		WeatherSnapshot commuteWeather = new WeatherSnapshot(20, 21, 10, PrecipitationType.NONE, 2.0);
		WeatherSnapshot leaveWorkWeather = new WeatherSnapshot(24, 25, 20, PrecipitationType.NONE, 2.5);

		when(userProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
		when(weatherSnapshotProvider.getTodayWeather(user, profile))
			.thenReturn(new WeatherSnapshots(commuteWeather, leaveWorkWeather));

		com.dailyoutfitweather.recommendation.dto.WeeklyRecommendationResponse response = recommendationService.getWeeklyRecommendation(user);

		assertThat(response.dailyForecasts()).hasSize(7);
		assertThat(response.dailyForecasts().get(0).outfitTags()).isNotEmpty();
	}

	private User devUser() {
		return new User(DEV_USER_EMAIL, "수진", AuthProvider.DEV, "dev-user");
	}

	private UserProfile profile(User user) {
		UserProfile profile = new UserProfile(user);
		profile.update(
			4,
			2,
			LocalTime.of(8, 30),
			LocalTime.of(18, 30),
			LocalTime.of(7, 30),
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.CHARACTER,
			ChangeAlertOption.IMPORTANT_ONLY
		);
		return profile;
	}

	private OutfitRecommendation recommendation(User user, LocalDate targetDate) {
		WeatherSnapshot commuteWeather = new WeatherSnapshot(17, 16, 20, PrecipitationType.NONE, 2.4);
		WeatherSnapshot leaveWorkWeather = new WeatherSnapshot(14, 13, 30, PrecipitationType.NONE, 2.8);
		return new OutfitRecommendation(
			user,
			targetDate,
			result(),
			RecommendationWeatherSnapshot.from(commuteWeather, leaveWorkWeather)
		);
	}

	private OutfitRecommendation staleRecommendation(User user, LocalDate targetDate) {
		WeatherSnapshot commuteWeather = new WeatherSnapshot(17, 16, 20, PrecipitationType.NONE, 2.4, 0);
		WeatherSnapshot leaveWorkWeather = new WeatherSnapshot(14, 13, 30, PrecipitationType.NONE, 2.8, 0);
		return new OutfitRecommendation(
			user,
			targetDate,
			result(),
			RecommendationWeatherSnapshot.from(commuteWeather, leaveWorkWeather)
		);
	}

	private RecommendationResult result() {
		return new RecommendationResult(
			13,
			"긴팔 티셔츠",
			"가벼운 재킷",
			"작은 우산",
			java.util.List.of("작은 우산"),
			"오늘은 가벼운 겉옷을 챙겨요.",
			"추천 기준 체감온도는 13도입니다.",
			"LIGHT_OUTER"
		);
	}
}
