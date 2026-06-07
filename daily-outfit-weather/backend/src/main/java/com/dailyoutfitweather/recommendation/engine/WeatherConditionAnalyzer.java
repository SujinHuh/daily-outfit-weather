package com.dailyoutfitweather.recommendation.engine;

import org.springframework.stereotype.Component;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.RecommendationInput;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;

@Component
public class WeatherConditionAnalyzer {

	private static final int RAIN_RECOMMENDATION_THRESHOLD = 60;
	private static final double STRONG_WIND_THRESHOLD = 4.0;

	public AnalyzedWeatherCondition analyze(RecommendationInput input) {
		WeatherSnapshot commute = input.commuteWeather();
		WeatherSnapshot leaveWork = input.leaveWorkWeather();
		int minFeelsLikeTemperature = Math.min(commute.feelsLikeTemperature(), leaveWork.feelsLikeTemperature());
		int maxFeelsLikeTemperature = Math.max(commute.feelsLikeTemperature(), leaveWork.feelsLikeTemperature());
		int maxRainProbability = Math.max(commute.rainProbability(), leaveWork.rainProbability());
		boolean snowExpected = hasPrecipitation(commute, PrecipitationType.SNOW)
			|| hasPrecipitation(leaveWork, PrecipitationType.SNOW);
		boolean leaveWorkRainExpected = isRainLike(leaveWork);
		boolean rainExpected = isRainLike(commute) || leaveWorkRainExpected;
		boolean strongWind = commute.windSpeed() >= STRONG_WIND_THRESHOLD
			|| leaveWork.windSpeed() >= STRONG_WIND_THRESHOLD;
		boolean leaveWorkColder = leaveWork.feelsLikeTemperature() <= commute.feelsLikeTemperature() - 3;
		PrecipitationType dominantPrecipitationType = snowExpected ? PrecipitationType.SNOW
			: rainExpected ? PrecipitationType.RAIN : PrecipitationType.NONE;

		return new AnalyzedWeatherCondition(
			commute.feelsLikeTemperature(),
			minFeelsLikeTemperature,
			maxFeelsLikeTemperature,
			maxRainProbability,
			rainExpected,
			leaveWorkRainExpected,
			snowExpected,
			strongWind,
			leaveWorkColder,
			dominantPrecipitationType
		);
	}

	private boolean hasPrecipitation(WeatherSnapshot snapshot, PrecipitationType precipitationType) {
		return snapshot.precipitationType() == precipitationType;
	}

	private boolean isRainLike(WeatherSnapshot snapshot) {
		return hasPrecipitation(snapshot, PrecipitationType.RAIN)
			|| (hasPrecipitation(snapshot, PrecipitationType.NONE)
				&& snapshot.rainProbability() >= RAIN_RECOMMENDATION_THRESHOLD);
	}
}
