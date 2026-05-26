package com.dailyoutfitweather.recommendation.engine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ItemSelector {

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
		return List.copyOf(items);
	}
}
