package com.dailyoutfitweather.recommendation.domain;

import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;

public record RecommendationWeatherSnapshot(
	WeatherSnapshot commuteWeather,
	WeatherSnapshot leaveWorkWeather,
	WeatherSummary weatherSummary
) {

	public static RecommendationWeatherSnapshot from(WeatherSnapshot commuteWeather, WeatherSnapshot leaveWorkWeather) {
		return new RecommendationWeatherSnapshot(
			commuteWeather,
			leaveWorkWeather,
			WeatherSummary.from(commuteWeather, leaveWorkWeather)
		);
	}
}
