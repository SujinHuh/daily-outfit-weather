package com.dailyoutfitweather.recommendation.engine;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;

record AnalyzedWeatherCondition(
	int baseFeelsLikeTemperature,
	int minFeelsLikeTemperature,
	int maxFeelsLikeTemperature,
	int maxRainProbability,
	int maxHumidity,
	boolean humidHeat,
	boolean rainExpected,
	boolean leaveWorkRainExpected,
	boolean snowExpected,
	boolean strongWind,
	boolean leaveWorkColder,
	PrecipitationType dominantPrecipitationType
) {
}
