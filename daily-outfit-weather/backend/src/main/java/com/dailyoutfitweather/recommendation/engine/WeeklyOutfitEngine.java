package com.dailyoutfitweather.recommendation.engine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class WeeklyOutfitEngine {

	public List<String> generateTags(int minTemp, int maxTemp, int rainProbability, String weatherCondition) {
		List<String> tags = new ArrayList<>();

		int representativeTemp = (minTemp + maxTemp) / 2;

		if (representativeTemp >= 28) {
			tags.add("반팔티");
			tags.add("시원한 하의");
		} else if (representativeTemp >= 24) {
			tags.add("반팔티");
			tags.add("얇은 셔츠");
		} else if (representativeTemp >= 21) {
			tags.add("얇은 긴팔");
			tags.add("가디건");
		} else if (representativeTemp >= 18) {
			tags.add("긴팔티");
			tags.add("자켓");
		} else if (representativeTemp >= 12) {
			tags.add("니트");
			tags.add("바람막이");
		} else if (representativeTemp >= 8) {
			tags.add("코트");
			tags.add("가디건");
		} else if (representativeTemp >= 4) {
			tags.add("니트");
			tags.add("패딩");
		} else {
			tags.add("두꺼운 패딩");
			tags.add("장갑/목도리");
		}

		if (rainProbability >= 60 || "RAIN".equalsIgnoreCase(weatherCondition)) {
			tags.add("우산 필수");
		} else if ("SNOW".equalsIgnoreCase(weatherCondition)) {
			tags.add("방한화/우산");
		}

		return tags;
	}
}
