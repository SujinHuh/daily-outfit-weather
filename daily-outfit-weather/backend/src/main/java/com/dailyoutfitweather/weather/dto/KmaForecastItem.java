package com.dailyoutfitweather.weather.dto;

public record KmaForecastItem(
	String category,
	String fcstDate,
	String fcstTime,
	String fcstValue
) {
}
