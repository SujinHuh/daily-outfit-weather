package com.dailyoutfitweather.recommendation.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyForecastSummary(
	LocalDate date,
	String dayOfWeek,
	int minTemperature,
	int maxTemperature,
	int rainProbability,
	String weatherCondition,
	List<String> outfitTags
) {}
