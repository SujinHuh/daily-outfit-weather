package com.dailyoutfitweather.recommendation.dto;

import java.util.Objects;

public record WeatherSnapshot(
	int temperature,
	int feelsLikeTemperature,
	int rainProbability,
	PrecipitationType precipitationType,
	double windSpeed
) {

	public WeatherSnapshot {
		Objects.requireNonNull(precipitationType, "precipitationType must not be null");
		if (rainProbability < 0 || rainProbability > 100) {
			throw new IllegalArgumentException("rainProbability must be between 0 and 100");
		}
		if (windSpeed < 0) {
			throw new IllegalArgumentException("windSpeed must be greater than or equal to 0");
		}
	}
}
