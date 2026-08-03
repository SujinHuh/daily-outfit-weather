package com.dailyoutfitweather.recommendation.dto;

import java.util.List;

public record WeeklyRecommendationResponse(
	List<DailyForecastSummary> dailyForecasts
) {}
