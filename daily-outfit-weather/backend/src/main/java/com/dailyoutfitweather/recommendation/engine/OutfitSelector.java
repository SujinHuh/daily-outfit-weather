package com.dailyoutfitweather.recommendation.engine;

import org.springframework.stereotype.Component;

@Component
public class OutfitSelector {

	public OutfitRecommendation select(int recommendationTemperature) {
		if (recommendationTemperature >= 28) {
			return new OutfitRecommendation("반팔", "얇은 하의", "HOT_LIGHT");
		}
		if (recommendationTemperature >= 23) {
			return new OutfitRecommendation("반팔 또는 얇은 셔츠", "가벼운 하의", "WARM_LIGHT");
		}
		if (recommendationTemperature >= 20) {
			return new OutfitRecommendation("얇은 긴팔", "셔츠", "MILD_LONG_SLEEVE");
		}
		if (recommendationTemperature >= 17) {
			return new OutfitRecommendation("얇은 니트", "가디건", "COOL_CARDIGAN");
		}
		if (recommendationTemperature >= 12) {
			return new OutfitRecommendation("니트", "바람막이 또는 자켓", "WINDY_LIGHT_OUTER");
		}
		if (recommendationTemperature >= 8) {
			return new OutfitRecommendation("긴팔 이너", "코트 또는 두꺼운 가디건", "COLD_COAT");
		}
		if (recommendationTemperature >= 4) {
			return new OutfitRecommendation("따뜻한 니트", "패딩 또는 두꺼운 코트", "VERY_COLD_PADDING");
		}
		return new OutfitRecommendation("기모 이너", "두꺼운 패딩, 목도리, 장갑", "FREEZING_PADDING");
	}
}
