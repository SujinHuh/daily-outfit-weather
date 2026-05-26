package com.dailyoutfitweather.weather.client;

import org.springframework.stereotype.Component;

@Component
public class FeelsLikeTemperatureCalculator {

	public int calculate(int temperature, double windSpeed) {
		if (windSpeed >= 4.0) {
			return temperature - 2;
		}
		if (windSpeed >= 2.0) {
			return temperature - 1;
		}
		return temperature;
	}
}
