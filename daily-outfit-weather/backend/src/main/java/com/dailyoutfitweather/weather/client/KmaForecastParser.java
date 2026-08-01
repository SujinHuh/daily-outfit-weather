package com.dailyoutfitweather.weather.client;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.weather.dto.KmaForecastItem;

@Component
public class KmaForecastParser {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	private final FeelsLikeTemperatureCalculator feelsLikeTemperatureCalculator;

	public KmaForecastParser(FeelsLikeTemperatureCalculator feelsLikeTemperatureCalculator) {
		this.feelsLikeTemperatureCalculator = feelsLikeTemperatureCalculator;
	}

	public WeatherSnapshot parse(List<KmaForecastItem> items, LocalDate targetDate, LocalTime targetTime) {
		String fcstDate = targetDate.format(DATE_FORMATTER);
		String fcstTime = KmaForecastTime.format(KmaForecastTime.targetForecastTime(targetTime));
		Map<String, String> values = new HashMap<>();
		for (KmaForecastItem item : items) {
			if (fcstDate.equals(item.fcstDate()) && fcstTime.equals(item.fcstTime())) {
				values.put(item.category(), item.fcstValue());
			}
		}

		int temperature = parseRoundedInt(required(values, "TMP"));
		int rainProbability = parseRoundedInt(values.getOrDefault("POP", "0"));
		double windSpeed = parseDouble(values.getOrDefault("WSD", "0"));
		int humidity = parseRoundedInt(values.getOrDefault("REH", "50"));
		PrecipitationType precipitationType = parsePrecipitationType(values.getOrDefault("PTY", "0"));
		int feelsLikeTemperature = feelsLikeTemperatureCalculator.calculate(temperature, windSpeed, humidity);
		return new WeatherSnapshot(temperature, feelsLikeTemperature, rainProbability, precipitationType, windSpeed, humidity);
	}

	private String required(Map<String, String> values, String category) {
		String value = values.get(category);
		if (value == null) {
			throw new WeatherApiException("Required KMA forecast category is missing: " + category);
		}
		return value;
	}

	private int parseRoundedInt(String value) {
		return (int)Math.round(parseDouble(value));
	}

	private double parseDouble(String value) {
		try {
			return Double.parseDouble(value);
		}
		catch (NumberFormatException exception) {
			throw new WeatherApiException("Invalid KMA forecast value: " + value, exception);
		}
	}

	private PrecipitationType parsePrecipitationType(String value) {
		return switch (value) {
			case "1", "4", "5" -> PrecipitationType.RAIN;
			case "2", "3", "6", "7" -> PrecipitationType.SNOW;
			default -> PrecipitationType.NONE;
		};
	}
}
