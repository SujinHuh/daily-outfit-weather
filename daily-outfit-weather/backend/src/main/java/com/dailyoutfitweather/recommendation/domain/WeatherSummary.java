package com.dailyoutfitweather.recommendation.domain;

import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;

public record WeatherSummary(
	int commuteFeelsLike,
	int leaveWorkFeelsLike,
	int rainProbability,
	double windSpeed,
	boolean rainExpected,
	boolean snowExpected
) {

	public static WeatherSummary from(WeatherSnapshot commuteWeather, WeatherSnapshot leaveWorkWeather) {
		return new WeatherSummary(
			commuteWeather.feelsLikeTemperature(),
			leaveWorkWeather.feelsLikeTemperature(),
			Math.max(commuteWeather.rainProbability(), leaveWorkWeather.rainProbability()),
			Math.max(commuteWeather.windSpeed(), leaveWorkWeather.windSpeed()),
			commuteWeather.precipitationType() == com.dailyoutfitweather.recommendation.dto.PrecipitationType.RAIN
				|| leaveWorkWeather.precipitationType() == com.dailyoutfitweather.recommendation.dto.PrecipitationType.RAIN
				|| Math.max(commuteWeather.rainProbability(), leaveWorkWeather.rainProbability()) >= 60,
			commuteWeather.precipitationType() == com.dailyoutfitweather.recommendation.dto.PrecipitationType.SNOW
				|| leaveWorkWeather.precipitationType() == com.dailyoutfitweather.recommendation.dto.PrecipitationType.SNOW
		);
	}
}
