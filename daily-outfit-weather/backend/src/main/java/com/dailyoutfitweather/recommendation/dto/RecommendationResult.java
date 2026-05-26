package com.dailyoutfitweather.recommendation.dto;

import java.util.List;

public record RecommendationResult(
	int recommendationTemperature,
	String topRecommendation,
	String outerRecommendation,
	String itemRecommendation,
	List<String> itemRecommendations,
	String summaryMessage,
	String reason,
	String characterImageType
) {
}
