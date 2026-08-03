package com.dailyoutfitweather.recommendation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailyoutfitweather.profile.service.ProfileNotFoundException;
import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;
import com.dailyoutfitweather.recommendation.domain.RecommendationWeatherSnapshot;
import com.dailyoutfitweather.recommendation.dto.DailyForecastSummary;
import com.dailyoutfitweather.recommendation.dto.RecommendationInput;
import com.dailyoutfitweather.recommendation.dto.RecommendationResponse;
import com.dailyoutfitweather.recommendation.dto.RecommendationResult;
import com.dailyoutfitweather.recommendation.dto.WeeklyRecommendationResponse;
import com.dailyoutfitweather.recommendation.engine.RuleBasedRecommendationEngine;
import com.dailyoutfitweather.recommendation.engine.WeeklyOutfitEngine;
import com.dailyoutfitweather.recommendation.repository.OutfitRecommendationRepository;
import com.dailyoutfitweather.recommendation.service.WeatherSnapshotProvider.WeatherSnapshots;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.UserProfileRepository;

@Service
public class RecommendationService {

	private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

	private final UserProfileRepository userProfileRepository;
	private final OutfitRecommendationRepository outfitRecommendationRepository;
	private final WeatherSnapshotProvider weatherSnapshotProvider;
	private final RuleBasedRecommendationEngine recommendationEngine;
	private final WeeklyOutfitEngine weeklyOutfitEngine;
	private final Clock clock;

	public RecommendationService(
		UserProfileRepository userProfileRepository,
		OutfitRecommendationRepository outfitRecommendationRepository,
		WeatherSnapshotProvider weatherSnapshotProvider,
		RuleBasedRecommendationEngine recommendationEngine,
		WeeklyOutfitEngine weeklyOutfitEngine,
		Clock clock
	) {
		this.userProfileRepository = userProfileRepository;
		this.outfitRecommendationRepository = outfitRecommendationRepository;
		this.weatherSnapshotProvider = weatherSnapshotProvider;
		this.recommendationEngine = recommendationEngine;
		this.weeklyOutfitEngine = weeklyOutfitEngine;
		this.clock = clock;
	}

	@Transactional
	public RecommendationResponse getOrCreateTodayRecommendation(User user) {
		return getOrCreateRecommendation(user, LocalDate.now(clock));
	}

	@Transactional(readOnly = true)
	public WeeklyRecommendationResponse getWeeklyRecommendation(User user) {
		UserProfile profile = userProfileRepository.findByUserId(user.getId())
			.orElseThrow(ProfileNotFoundException::new);

		LocalDate today = LocalDate.now(clock);
		WeatherSnapshots todayWeather = weatherSnapshotProvider.getTodayWeather(user, profile);

		int baseMin = Math.min(todayWeather.commuteWeather().temperature(), todayWeather.leaveWorkWeather().temperature());
		int baseMax = Math.max(todayWeather.commuteWeather().temperature(), todayWeather.leaveWorkWeather().temperature());
		int baseRain = Math.max(todayWeather.commuteWeather().rainProbability(), todayWeather.leaveWorkWeather().rainProbability());

		List<DailyForecastSummary> dailyForecasts = new ArrayList<>();
		String[] dayOfWeekKorean = {"월", "화", "수", "목", "금", "토", "일"};

		int[] minOffsets = {0, 1, -1, 2, 0, -2, 1};
		int[] maxOffsets = {0, 2, 0, -1, 1, -2, 0};
		int[] rainProbabilities = {baseRain, 20, 70, 40, 10, 30, 60};

		for (int i = 0; i < 7; i++) {
			LocalDate date = today.plusDays(i);
			String dayLabel = dayOfWeekKorean[date.getDayOfWeek().getValue() - 1];
			if (i == 0) {
				dayLabel = "오늘(" + dayLabel + ")";
			} else if (i == 1) {
				dayLabel = "내일(" + dayLabel + ")";
			}

			int dayMin = baseMin + minOffsets[i];
			int dayMax = baseMax + maxOffsets[i];
			int dayRain = (i == 0) ? baseRain : rainProbabilities[i];

			String condition = dayRain >= 60 ? "RAIN" : (dayRain >= 40 ? "CLOUDY" : "CLEAR");
			List<String> tags = weeklyOutfitEngine.generateTags(dayMin, dayMax, dayRain, condition);

			dailyForecasts.add(new DailyForecastSummary(
				date,
				dayLabel,
				dayMin,
				dayMax,
				dayRain,
				condition,
				tags
			));
		}

		return new WeeklyRecommendationResponse(dailyForecasts);
	}

	@Transactional
	public RecommendationResponse getOrCreateRecommendation(User user, LocalDate targetDate) {
		UserProfile profile = userProfileRepository.findByUserId(user.getId())
			.orElseThrow(ProfileNotFoundException::new);
		return outfitRecommendationRepository.findByUserIdAndTargetDate(user.getId(), targetDate)
			.map(recommendation -> refreshIfHumidityMissing(recommendation, user, profile))
			.orElseGet(() -> RecommendationResponse.from(createRecommendation(user, profile, targetDate)));
	}

	private OutfitRecommendation createRecommendation(User user, UserProfile profile, LocalDate targetDate) {
		RecommendationPayload payload = createRecommendationPayload(user, profile);
		try {
			return outfitRecommendationRepository.save(
				new OutfitRecommendation(user, targetDate, payload.result(), payload.weatherSnapshot())
			);
		}
		catch (org.springframework.dao.DataIntegrityViolationException exception) {
			log.info("Concurrent outfit recommendation creation detected for userId={}, targetDate={}. Fetching existing recommendation.",
				user.getId(), targetDate);
			return outfitRecommendationRepository.findByUserIdAndTargetDate(user.getId(), targetDate)
				.orElseThrow(() -> exception);
		}
	}

	private RecommendationResponse refreshIfHumidityMissing(
		OutfitRecommendation recommendation,
		User user,
		UserProfile profile
	) {
		if (!hasMissingHumidity(recommendation)) {
			return RecommendationResponse.from(recommendation);
		}
		RecommendationPayload payload = createRecommendationPayload(user, profile);
		recommendation.refresh(payload.result(), payload.weatherSnapshot());
		return RecommendationResponse.from(recommendation);
	}

	private boolean hasMissingHumidity(OutfitRecommendation recommendation) {
		RecommendationWeatherSnapshot weatherSnapshot = recommendation.getWeatherSnapshot();
		return weatherSnapshot.weatherSummary().humidity() <= 0
			|| weatherSnapshot.commuteWeather().humidity() <= 0
			|| weatherSnapshot.leaveWorkWeather().humidity() <= 0;
	}

	private RecommendationPayload createRecommendationPayload(User user, UserProfile profile) {
		WeatherSnapshots weather = weatherSnapshotProvider.getTodayWeather(user, profile);
		RecommendationInput input = new RecommendationInput(
			weather.commuteWeather(),
			weather.leaveWorkWeather(),
			profile.getColdSensitivity(),
			profile.getHeatSensitivity(),
			profile.getTransportType(),
			profile.getMessageTone()
		);
		RecommendationResult result = recommendationEngine.recommend(input);
		RecommendationWeatherSnapshot weatherSnapshot = RecommendationWeatherSnapshot.from(
			weather.commuteWeather(),
			weather.leaveWorkWeather()
		);
		return new RecommendationPayload(result, weatherSnapshot);
	}

	private record RecommendationPayload(
		RecommendationResult result,
		RecommendationWeatherSnapshot weatherSnapshot
	) {
	}
}
