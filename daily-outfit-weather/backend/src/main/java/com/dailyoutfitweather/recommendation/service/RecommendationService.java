package com.dailyoutfitweather.recommendation.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailyoutfitweather.profile.service.ProfileNotFoundException;
import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;
import com.dailyoutfitweather.recommendation.domain.RecommendationWeatherSnapshot;
import com.dailyoutfitweather.recommendation.dto.RecommendationInput;
import com.dailyoutfitweather.recommendation.dto.RecommendationResponse;
import com.dailyoutfitweather.recommendation.dto.RecommendationResult;
import com.dailyoutfitweather.recommendation.engine.RuleBasedRecommendationEngine;
import com.dailyoutfitweather.recommendation.repository.OutfitRecommendationRepository;
import com.dailyoutfitweather.recommendation.service.WeatherSnapshotProvider.WeatherSnapshots;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.UserProfileRepository;

@Service
public class RecommendationService {

	private final UserProfileRepository userProfileRepository;
	private final OutfitRecommendationRepository outfitRecommendationRepository;
	private final WeatherSnapshotProvider weatherSnapshotProvider;
	private final RuleBasedRecommendationEngine recommendationEngine;
	private final Clock clock;

	public RecommendationService(
		UserProfileRepository userProfileRepository,
		OutfitRecommendationRepository outfitRecommendationRepository,
		WeatherSnapshotProvider weatherSnapshotProvider,
		RuleBasedRecommendationEngine recommendationEngine,
		Clock clock
	) {
		this.userProfileRepository = userProfileRepository;
		this.outfitRecommendationRepository = outfitRecommendationRepository;
		this.weatherSnapshotProvider = weatherSnapshotProvider;
		this.recommendationEngine = recommendationEngine;
		this.clock = clock;
	}

	@Transactional
	public RecommendationResponse getOrCreateTodayRecommendation(User user) {
		return getOrCreateRecommendation(user, LocalDate.now(clock));
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
		return outfitRecommendationRepository.save(
			new OutfitRecommendation(user, targetDate, payload.result(), payload.weatherSnapshot())
		);
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
