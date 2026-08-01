package com.dailyoutfitweather.notification.service;

import java.util.ArrayList;
import java.util.List;

import com.dailyoutfitweather.recommendation.domain.WeatherSummary;
import com.dailyoutfitweather.recommendation.dto.RecommendationResponse;

final class NotificationRecommendationSummary {

	private NotificationRecommendationSummary() {
	}

	static String body(RecommendationResponse recommendation) {
		WeatherSummary weather = recommendation.weatherSummary();
		String weatherText = "출근 체감 " + weather.commuteFeelsLike()
			+ "도, 퇴근 체감 " + weather.leaveWorkFeelsLike()
			+ "도, 강수확률 " + weather.rainProbability() + "%";
		return weatherText + " - " + String.join(", ", outfitItems(recommendation));
	}

	private static List<String> outfitItems(RecommendationResponse recommendation) {
		List<String> items = new ArrayList<>();
		addIfPresent(items, recommendation.topRecommendation());
		addIfPresent(items, recommendation.outerRecommendation());
		addIfPresent(items, recommendation.itemRecommendation());
		if (items.isEmpty()) {
			addIfPresent(items, recommendation.summaryMessage());
		}
		return items;
	}

	private static void addIfPresent(List<String> items, String value) {
		if (value != null && !value.isBlank()) {
			items.add(value);
		}
	}
}
