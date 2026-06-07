package com.dailyoutfitweather.recommendation.engine;

import org.springframework.stereotype.Component;

@Component
public class OutfitSelector {

	public OutfitRecommendation select(int recommendationTemperature, boolean strongWind) {
		if (recommendationTemperature >= 31) {
			return new OutfitRecommendation("시원한 반팔티", "통기성 좋은 얇은 하의", "HOT_LIGHT");
		}
		if (recommendationTemperature >= 28) {
			return new OutfitRecommendation("반팔티", "얇은 하의", "HOT_LIGHT");
		}
		if (recommendationTemperature >= 24) {
			return new OutfitRecommendation("반팔티", strongWind ? "얇은 바람막이" : "얇은 남방", "WARM_LIGHT");
		}
		if (recommendationTemperature >= 21) {
			return new OutfitRecommendation("얇은 긴팔티", strongWind ? "얇은 바람막이" : "얇은 남방", "MILD_LONG_SLEEVE");
		}
		if (recommendationTemperature >= 18) {
			return new OutfitRecommendation("긴팔티", strongWind ? "바람막이" : "가디건", "COOL_CARDIGAN");
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
