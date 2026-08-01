package com.dailyoutfitweather.recommendation.engine;

import org.springframework.stereotype.Component;

@Component
public class RecommendationMessageGenerator {

	public String generate(
		AnalyzedWeatherCondition condition,
		int recommendationTemperature
	) {
		if (condition.snowExpected()) {
			return "눈이 와요. 따뜻하게 입어요.";
		}
		if (condition.rainExpected()) {
			return "비가 와요. 비에 젖지 않게 가볍게 입어요.";
		}
		if (condition.strongWind()) {
			return "바람이 많이 불어요. 바람막이를 챙겨요.";
		}
		if (condition.humidHeat() && recommendationTemperature >= 31) {
			return "후텁지근하게 더워요. 시원하게 입고 물을 챙겨요.";
		}
		if (condition.humidHeat() && recommendationTemperature >= 28) {
			return "습해서 더 덥게 느껴져요. 가볍게 입어요.";
		}
		if (recommendationTemperature >= 31) {
			return "많이 더워요. 시원한 반팔티로 입어요.";
		}
		if (recommendationTemperature >= 28) {
			return "더워요. 반팔티로 가볍게 입어요.";
		}
		if (recommendationTemperature >= 24) {
			return "따뜻해요. 반팔티에 얇은 남방으로 입어요.";
		}
		if (recommendationTemperature >= 21) {
			return "선선해요. 얇은 긴팔티로 입어요.";
		}
		if (recommendationTemperature >= 18) {
			return "조금 쌀쌀해요. 긴팔티에 가디건을 챙겨요.";
		}
		if (recommendationTemperature >= 12) {
			return "쌀쌀해요. 니트에 바람막이 또는 자켓을 챙겨요.";
		}
		if (recommendationTemperature >= 8) {
			return "추워요. 긴팔 이너에 코트 또는 두꺼운 가디건을 챙겨요.";
		}
		if (recommendationTemperature >= 4) {
			return "많이 추워요. 따뜻한 니트에 패딩 또는 두꺼운 코트를 챙겨요.";
		}
		return "매우 추워요. 기모 이너와 두꺼운 패딩으로 입어요.";
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
		if (condition.humidHeat()) {
			builder.append(" 습도 ").append(condition.maxHumidity()).append("%로 후텁지근함을 반영했습니다.");
		}
		if (condition.rainExpected()) {
			builder.append(" 강수 가능성을 반영했습니다.");
		}
		if (condition.snowExpected()) {
			builder.append(" 눈 예보를 반영했습니다.");
		}
		if (condition.maxFeelsLikeTemperature() >= 31) {
			builder.append(" 많이 더워 땀이 날 수 있으니 수분을 자주 보충하세요.");
		} else if (condition.maxFeelsLikeTemperature() >= 28) {
			builder.append(" 더운 시간대에는 수분을 챙기세요.");
		}
		return builder.toString();
	}
}
