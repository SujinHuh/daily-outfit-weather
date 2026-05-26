package com.dailyoutfitweather.recommendation.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.RecommendationInput;
import com.dailyoutfitweather.recommendation.dto.RecommendationResult;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.user.domain.MessageTone;
import com.dailyoutfitweather.user.domain.TransportType;

class RuleBasedRecommendationEngineTest {

	private RuleBasedRecommendationEngine engine;

	@BeforeEach
	void setUp() {
		engine = new RuleBasedRecommendationEngine(
			new WeatherConditionAnalyzer(),
			new RecommendationRuleEngine(),
			new OutfitSelector(),
			new ItemSelector(),
			new RecommendationMessageGenerator()
		);
	}

	@Test
	void recommendLightClothesForHotFeelsLikeTemperature() {
		RecommendationResult result = engine.recommend(input(
			weather(30, 30, 10, PrecipitationType.NONE, 1.0),
			weather(29, 29, 10, PrecipitationType.NONE, 1.0),
			2,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.PRACTICAL
		));

		assertThat(result.recommendationTemperature()).isGreaterThanOrEqualTo(28);
		assertThat(result.topRecommendation()).contains("반팔");
		assertThat(result.itemRecommendations()).isEmpty();
	}

	@Test
	void recommendOuterForColdSensitiveAndWindyDayWithoutDoubleCountingLeaveWorkDrop() {
		RecommendationResult result = engine.recommend(input(
			weather(15, 15, 10, PrecipitationType.NONE, 4.5),
			weather(10, 10, 20, PrecipitationType.NONE, 4.2),
			5,
			1,
			TransportType.WALK,
			MessageTone.PRACTICAL
		));

		assertThat(result.recommendationTemperature()).isEqualTo(9);
		assertThat(result.outerRecommendation()).contains("코트");
		assertThat(result.itemRecommendations()).contains("바람막이");
		assertThat(result.reason()).contains("강한 바람");
	}

	@Test
	void recommendUmbrellaForHighRainProbability() {
		RecommendationResult result = engine.recommend(input(
			weather(21, 21, 20, PrecipitationType.NONE, 1.0),
			weather(18, 18, 70, PrecipitationType.RAIN, 1.0),
			2,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.FRIENDLY
		));

		assertThat(result.itemRecommendations()).contains("작은 우산");
		assertThat(result.itemRecommendation()).isEqualTo("작은 우산");
		assertThat(result.summaryMessage()).contains("작은 우산");
	}

	@Test
	void recommendSmallUmbrellaForLeaveWorkRainWithoutTemperatureDrop() {
		RecommendationResult result = engine.recommend(input(
			weather(21, 21, 20, PrecipitationType.NONE, 1.0),
			weather(21, 21, 70, PrecipitationType.RAIN, 1.0),
			2,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.FRIENDLY
		));

		assertThat(result.itemRecommendations()).containsExactly("작은 우산");
	}

	@Test
	void recommendRegularUmbrellaForCommuteOnlyRainEvenWhenLeaveWorkIsColder() {
		RecommendationResult result = engine.recommend(input(
			weather(21, 21, 70, PrecipitationType.RAIN, 1.0),
			weather(17, 17, 10, PrecipitationType.NONE, 1.0),
			2,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.FRIENDLY
		));

		assertThat(result.itemRecommendations()).containsExactly("우산");
	}

	@Test
	void recommendSnowSafetyItemForSnow() {
		RecommendationResult result = engine.recommend(input(
			weather(3, 1, 70, PrecipitationType.SNOW, 2.0),
			weather(2, 0, 80, PrecipitationType.SNOW, 2.0),
			3,
			1,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.CHARACTER
		));

		assertThat(result.itemRecommendations()).contains("미끄럼 주의 신발");
		assertThat(result.itemRecommendations()).doesNotContain("우산", "작은 우산");
		assertThat(result.reason()).contains("눈 예보");
	}

	@Test
	void applyThresholdEdgesForRainAndWind() {
		RecommendationResult result = engine.recommend(input(
			weather(20, 20, 60, PrecipitationType.NONE, 4.0),
			weather(20, 20, 10, PrecipitationType.NONE, 1.0),
			2,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.PRACTICAL
		));

		assertThat(result.itemRecommendations()).contains("우산", "바람막이");
		assertThat(result.recommendationTemperature()).isEqualTo(18);
	}

	@Test
	void rejectInvalidRecommendationInput() {
		assertThatThrownBy(() -> input(
			null,
			weather(20, 20, 10, PrecipitationType.NONE, 1.0),
			2,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.PRACTICAL
		)).isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> input(
			weather(20, 20, 10, PrecipitationType.NONE, 1.0),
			weather(20, 20, 10, PrecipitationType.NONE, 1.0),
			0,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.PRACTICAL
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejectInvalidWeatherSnapshot() {
		assertThatThrownBy(() -> weather(20, 20, 101, PrecipitationType.NONE, 1.0))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> weather(20, 20, 10, null, 1.0))
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> weather(20, 20, 10, PrecipitationType.NONE, -0.1))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void changeMessageByTone() {
		RecommendationInput baseInput = input(
			weather(18, 18, 10, PrecipitationType.NONE, 1.0),
			weather(18, 18, 10, PrecipitationType.NONE, 1.0),
			2,
			2,
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.CHARACTER
		);

		String characterMessage = engine.recommend(baseInput).summaryMessage();
		String practicalMessage = engine.recommend(input(
			baseInput.commuteWeather(),
			baseInput.leaveWorkWeather(),
			baseInput.coldSensitivity(),
			baseInput.heatSensitivity(),
			baseInput.transportType(),
			MessageTone.PRACTICAL
		)).summaryMessage();

		assertThat(characterMessage).isNotEqualTo(practicalMessage);
		assertThat(practicalMessage).startsWith("권장 복장");
	}

	private RecommendationInput input(
		WeatherSnapshot commuteWeather,
		WeatherSnapshot leaveWorkWeather,
		int coldSensitivity,
		int heatSensitivity,
		TransportType transportType,
		MessageTone messageTone
	) {
		return new RecommendationInput(
			commuteWeather,
			leaveWorkWeather,
			coldSensitivity,
			heatSensitivity,
			transportType,
			messageTone
		);
	}

	private WeatherSnapshot weather(
		int temperature,
		int feelsLikeTemperature,
		int rainProbability,
		PrecipitationType precipitationType,
		double windSpeed
	) {
		return new WeatherSnapshot(temperature, feelsLikeTemperature, rainProbability, precipitationType, windSpeed);
	}
}
