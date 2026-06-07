package com.dailyoutfitweather.recommendation.engine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ItemSelector {

	private static final int MAX_RECOMMENDATION_COUNT = 3;

	public List<String> select(AnalyzedWeatherCondition condition) {
		List<String> items = new ArrayList<>();
		if (condition.snowExpected()) {
			items.add("미끄럼 주의 신발");
		}
		if (condition.rainExpected()) {
			items.add(condition.leaveWorkRainExpected() ? "작은 우산" : "우산");
		}
		if (condition.strongWind()) {
			items.add("바람막이");
		}
		if (condition.maxFeelsLikeTemperature() >= 31) {
			items.add("손선풍기");
		}
		if (condition.maxFeelsLikeTemperature() >= 28) {
			items.add("물");
		}
		return items.stream().limit(MAX_RECOMMENDATION_COUNT).toList();
	}
}
