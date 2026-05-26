package com.dailyoutfitweather.weather.client;

import java.time.LocalTime;

final class KmaForecastTime {

	private KmaForecastTime() {
	}

	static LocalTime targetForecastTime(LocalTime targetTime) {
		if (targetTime.getMinute() == 0 && targetTime.getSecond() == 0 && targetTime.getNano() == 0) {
			return targetTime.withMinute(0).withSecond(0).withNano(0);
		}
		return targetTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);
	}

	static String format(LocalTime time) {
		return "%02d00".formatted(time.getHour());
	}
}
