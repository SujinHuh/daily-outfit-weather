package com.dailyoutfitweather.recommendation.domain;

import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;

public record WeatherSummary(
	int commuteFeelsLike,
	int leaveWorkFeelsLike,
	int rainProbability,
	double windSpeed
) {

	public static WeatherSummary from(WeatherSnapshot commuteWeather, WeatherSnapshot leaveWorkWeather) {
		return new WeatherSummary(
			commuteWeather.feelsLikeTemperature(),
			leaveWorkWeather.feelsLikeTemperature(),
			Math.max(commuteWeather.rainProbability(), leaveWorkWeather.rainProbability()),
			Math.max(commuteWeather.windSpeed(), leaveWorkWeather.windSpeed())
		);
	}
}
