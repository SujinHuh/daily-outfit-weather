package com.dailyoutfitweather.recommendation.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class WeeklyOutfitEngineTest {

	private final WeeklyOutfitEngine weeklyOutfitEngine = new WeeklyOutfitEngine();

	@Test
	void generatesShortsAndTshirtForHotWeather() {
		List<String> tags = weeklyOutfitEngine.generateTags(26, 32, 10, "CLEAR");

		assertThat(tags).contains("반팔티", "시원한 하의");
	}

	@Test
	void generatesCardiganAndUmbrellaForCoolRainyWeather() {
		List<String> tags = weeklyOutfitEngine.generateTags(16, 22, 80, "RAIN");

		assertThat(tags).contains("긴팔티", "자켓", "우산 필수");
	}

	@Test
	void generatesPaddingForFreezingWeather() {
		List<String> tags = weeklyOutfitEngine.generateTags(-5, 2, 20, "SNOW");

		assertThat(tags).contains("두꺼운 패딩", "장갑/목도리", "방한화/우산");
	}
}
