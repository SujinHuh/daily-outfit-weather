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
import com.dailyoutfitweather.user.repository.UserRepository;

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
		UserProfile profile = userProfileRepository.findByUserId(user.getId())
			.orElseThrow(ProfileNotFoundException::new);
		LocalDate targetDate = LocalDate.now(clock);

		return outfitRecommendationRepository.findByUserIdAndTargetDate(user.getId(), targetDate)
			.map(RecommendationResponse::from)
			.orElseGet(() -> RecommendationResponse.from(createRecommendation(user, profile, targetDate)));
	}

	private OutfitRecommendation createRecommendation(User user, UserProfile profile, LocalDate targetDate) {
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
		return outfitRecommendationRepository.save(new OutfitRecommendation(user, targetDate, result, weatherSnapshot));
	}
}
