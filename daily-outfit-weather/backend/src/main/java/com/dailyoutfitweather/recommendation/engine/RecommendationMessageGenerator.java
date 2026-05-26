package com.dailyoutfitweather.recommendation.engine;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dailyoutfitweather.user.domain.MessageTone;

@Component
public class RecommendationMessageGenerator {

	public String generate(
		MessageTone messageTone,
		OutfitRecommendation outfit,
		List<String> items,
		AnalyzedWeatherCondition condition
	) {
		String itemMessage = items.isEmpty() ? "" : " 준비물은 " + String.join(", ", items) + "을 챙기세요.";
		return switch (messageTone) {
			case CHARACTER -> "오늘 날씨에 맞춰 " + outfit.topRecommendation() + "에 " + outfit.outerRecommendation()
				+ " 조합이 좋아요." + itemMessage;
			case FRIENDLY -> "오늘은 " + outfit.topRecommendation() + "와 " + outfit.outerRecommendation()
				+ "로 편하게 입어보세요." + itemMessage;
			case PRACTICAL -> "권장 복장은 " + outfit.topRecommendation() + ", " + outfit.outerRecommendation()
				+ "입니다." + itemMessage;
		};
	}

	public String reason(AnalyzedWeatherCondition condition, int recommendationTemperature) {
		StringBuilder builder = new StringBuilder();
		builder.append("추천 기준 체감온도는 ").append(recommendationTemperature).append("도입니다.");
		if (condition.leaveWorkColder()) {
			builder.append(" 퇴근길 체감온도 하락을 반영했습니다.");
		}
		if (condition.strongWind()) {
			builder.append(" 강한 바람으로 체감온도를 낮게 보정했습니다.");
		}
		if (condition.rainExpected()) {
			builder.append(" 강수 가능성을 반영했습니다.");
		}
		if (condition.snowExpected()) {
			builder.append(" 눈 예보를 반영했습니다.");
		}
		return builder.toString();
	}
}
