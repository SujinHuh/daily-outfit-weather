package com.dailyoutfitweather.recommendation.engine;

import org.springframework.stereotype.Component;

import com.dailyoutfitweather.recommendation.dto.RecommendationInput;
import com.dailyoutfitweather.user.domain.TransportType;

@Component
public class RecommendationRuleEngine {

	public int calculateRecommendationTemperature(RecommendationInput input, AnalyzedWeatherCondition condition) {
		int adjustedTemperature = condition.baseFeelsLikeTemperature();
		if (condition.strongWind()) {
			adjustedTemperature -= 2;
		}
		if (input.coldSensitivity() >= 4) {
			adjustedTemperature -= 2;
		}
		if (input.heatSensitivity() >= 4) {
			adjustedTemperature += 1;
		}
		if (input.transportType() == TransportType.WALK || input.transportType() == TransportType.BICYCLE) {
			adjustedTemperature -= 1;
		}
		if (condition.leaveWorkColder()) {
			adjustedTemperature -= 1;
		}
		return adjustedTemperature;
	}
}
