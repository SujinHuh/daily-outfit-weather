package com.dailyoutfitweather.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.weather.client.FeelsLikeTemperatureCalculator;
import com.dailyoutfitweather.weather.client.KmaForecastParser;
import com.dailyoutfitweather.weather.client.WeatherApiException;
import com.dailyoutfitweather.weather.dto.KmaForecastItem;

class KmaForecastParserTest {

	private final KmaForecastParser parser = new KmaForecastParser(new FeelsLikeTemperatureCalculator());

	@Test
	void parsesForecastItemsForTargetDateAndTime() {
		WeatherSnapshot snapshot = parser.parse(
			List.of(
				new KmaForecastItem("TMP", "20260525", "0800", "17"),
				new KmaForecastItem("TMP", "20260525", "0900", "20"),
				new KmaForecastItem("POP", "20260525", "0900", "70"),
				new KmaForecastItem("PTY", "20260525", "0900", "1"),
				new KmaForecastItem("WSD", "20260525", "0900", "4.2"),
				new KmaForecastItem("REH", "20260525", "0900", "65")
			),
			LocalDate.of(2026, 5, 25),
			LocalTime.of(8, 30)
		);

		assertThat(snapshot.temperature()).isEqualTo(20);
		assertThat(snapshot.feelsLikeTemperature()).isEqualTo(18);
		assertThat(snapshot.rainProbability()).isEqualTo(70);
		assertThat(snapshot.precipitationType()).isEqualTo(PrecipitationType.RAIN);
		assertThat(snapshot.windSpeed()).isEqualTo(4.2);
		assertThat(snapshot.humidity()).isEqualTo(65);
	}

	@Test
	void raisesFeelsLikeTemperatureForHumidHeat() {
		WeatherSnapshot snapshot = parser.parse(
			List.of(
				new KmaForecastItem("TMP", "20260525", "1500", "31"),
				new KmaForecastItem("POP", "20260525", "1500", "10"),
				new KmaForecastItem("PTY", "20260525", "1500", "0"),
				new KmaForecastItem("WSD", "20260525", "1500", "1.0"),
				new KmaForecastItem("REH", "20260525", "1500", "82")
			),
			LocalDate.of(2026, 5, 25),
			LocalTime.of(14, 30)
		);

		assertThat(snapshot.temperature()).isEqualTo(31);
		assertThat(snapshot.feelsLikeTemperature()).isEqualTo(34);
		assertThat(snapshot.humidity()).isEqualTo(82);
	}

	@Test
	void mapsSnowPrecipitationTypes() {
		WeatherSnapshot snapshot = parser.parse(
			List.of(
				new KmaForecastItem("TMP", "20260525", "1900", "2"),
				new KmaForecastItem("POP", "20260525", "1900", "60"),
				new KmaForecastItem("PTY", "20260525", "1900", "3"),
				new KmaForecastItem("WSD", "20260525", "1900", "1.0")
			),
			LocalDate.of(2026, 5, 25),
			LocalTime.of(18, 30)
		);

		assertThat(snapshot.precipitationType()).isEqualTo(PrecipitationType.SNOW);
	}

	@Test
	void mapsRaindropsAndSnowFlurries() {
		WeatherSnapshot raindrops = parser.parse(
			List.of(
				new KmaForecastItem("TMP", "20260525", "0900", "20"),
				new KmaForecastItem("PTY", "20260525", "0900", "5")
			),
			LocalDate.of(2026, 5, 25),
			LocalTime.of(8, 30)
		);
		WeatherSnapshot mixedFlurries = parser.parse(
			List.of(
				new KmaForecastItem("TMP", "20260525", "0900", "2"),
				new KmaForecastItem("PTY", "20260525", "0900", "6")
			),
			LocalDate.of(2026, 5, 25),
			LocalTime.of(8, 30)
		);
		WeatherSnapshot snowFlurries = parser.parse(
			List.of(
				new KmaForecastItem("TMP", "20260525", "0900", "2"),
				new KmaForecastItem("PTY", "20260525", "0900", "7")
			),
			LocalDate.of(2026, 5, 25),
			LocalTime.of(8, 30)
		);

		assertThat(raindrops.precipitationType()).isEqualTo(PrecipitationType.RAIN);
		assertThat(mixedFlurries.precipitationType()).isEqualTo(PrecipitationType.SNOW);
		assertThat(snowFlurries.precipitationType()).isEqualTo(PrecipitationType.SNOW);
	}

	@Test
	void failsWhenRequiredTemperatureIsMissing() {
		assertThatThrownBy(() -> parser.parse(
			List.of(new KmaForecastItem("POP", "20260525", "0800", "70")),
			LocalDate.of(2026, 5, 25),
			LocalTime.of(8, 30)
		)).isInstanceOf(WeatherApiException.class);
	}
}
