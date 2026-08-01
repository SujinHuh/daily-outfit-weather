package com.dailyoutfitweather.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.dailyoutfitweather.recommendation.domain.WeatherSummary;
import com.dailyoutfitweather.recommendation.dto.RecommendationResponse;

class NotificationRecommendationSummaryTest {

	@Test
	void bodyCombinesWeatherAndOutfitInOneLine() {
		RecommendationResponse recommendation = new RecommendationResponse(
			1L,
			LocalDate.of(2026, 5, 25),
			"오늘은 가벼운 겉옷을 챙겨요.",
			"LIGHT_OUTER",
			"긴팔 티셔츠",
			"가벼운 재킷",
			"작은 우산",
			"추천 기준 체감온도는 13도입니다.",
			new WeatherSummary(16, 13, 30, 2.8, false, false)
		);

		String body = NotificationRecommendationSummary.body(recommendation);

		assertThat(body).isEqualTo("출근 체감 16도, 퇴근 체감 13도, 강수확률 30% - 긴팔 티셔츠, 가벼운 재킷, 작은 우산");
		assertThat(body).doesNotContain("\n");
	}
}
