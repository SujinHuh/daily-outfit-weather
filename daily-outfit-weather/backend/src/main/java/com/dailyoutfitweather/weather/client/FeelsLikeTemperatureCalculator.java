package com.dailyoutfitweather.weather.client;

import org.springframework.stereotype.Component;

@Component
public class FeelsLikeTemperatureCalculator {

	public int calculate(int temperature, double windSpeed) {
		return calculate(temperature, windSpeed, 50);
	}

	public int calculate(int temperature, double windSpeed, int humidity) {
		int feelsLikeTemperature = temperature;
		if (windSpeed >= 4.0) {
			feelsLikeTemperature -= 2;
		} else if (windSpeed >= 2.0) {
			feelsLikeTemperature -= 1;
		}

		if (temperature >= 30 && humidity >= 80) {
			feelsLikeTemperature += 3;
		} else if (temperature >= 30 && humidity >= 70) {
			feelsLikeTemperature += 2;
		} else if (temperature >= 27 && humidity >= 80) {
			feelsLikeTemperature += 2;
		} else if (temperature >= 27 && humidity >= 70) {
			feelsLikeTemperature += 1;
		}
		return feelsLikeTemperature;
	}
}
