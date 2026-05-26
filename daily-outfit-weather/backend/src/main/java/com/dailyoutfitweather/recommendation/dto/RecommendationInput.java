package com.dailyoutfitweather.recommendation.dto;

import java.util.Objects;

import com.dailyoutfitweather.user.domain.MessageTone;
import com.dailyoutfitweather.user.domain.TransportType;

public record RecommendationInput(
	WeatherSnapshot commuteWeather,
	WeatherSnapshot leaveWorkWeather,
	int coldSensitivity,
	int heatSensitivity,
	TransportType transportType,
	MessageTone messageTone
) {

	public RecommendationInput {
		Objects.requireNonNull(commuteWeather, "commuteWeather must not be null");
		Objects.requireNonNull(leaveWorkWeather, "leaveWorkWeather must not be null");
		Objects.requireNonNull(transportType, "transportType must not be null");
		Objects.requireNonNull(messageTone, "messageTone must not be null");
		if (coldSensitivity < 1 || coldSensitivity > 5) {
			throw new IllegalArgumentException("coldSensitivity must be between 1 and 5");
		}
		if (heatSensitivity < 1 || heatSensitivity > 5) {
			throw new IllegalArgumentException("heatSensitivity must be between 1 and 5");
		}
	}
}
