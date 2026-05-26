package com.dailyoutfitweather.recommendation.dto;

import java.time.LocalDate;

import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;
import com.dailyoutfitweather.recommendation.domain.WeatherSummary;

public record RecommendationResponse(
	Long id,
	LocalDate targetDate,
	String summaryMessage,
	String characterImageType,
	String topRecommendation,
	String outerRecommendation,
	String itemRecommendation,
	String reason,
	WeatherSummary weatherSummary
) {

	public static RecommendationResponse from(OutfitRecommendation recommendation) {
		return new RecommendationResponse(
			recommendation.getId(),
			recommendation.getTargetDate(),
			recommendation.getSummaryMessage(),
			recommendation.getCharacterImageType(),
			recommendation.getTopRecommendation(),
			recommendation.getOuterRecommendation(),
			recommendation.getItemRecommendation(),
			recommendation.getReason(),
			recommendation.getWeatherSnapshot().weatherSummary()
		);
	}
}
