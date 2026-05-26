package com.dailyoutfitweather.recommendation.engine;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;

record AnalyzedWeatherCondition(
	int baseFeelsLikeTemperature,
	int minFeelsLikeTemperature,
	int maxRainProbability,
	boolean rainExpected,
	boolean leaveWorkRainExpected,
	boolean snowExpected,
	boolean strongWind,
	boolean leaveWorkColder,
	PrecipitationType dominantPrecipitationType
) {
}
