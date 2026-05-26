package com.dailyoutfitweather.weather.client;

import java.time.LocalDate;
import java.time.LocalTime;

import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;

public interface WeatherApiClient {

	WeatherSnapshot getForecast(int nx, int ny, LocalDate targetDate, LocalTime targetTime);
}
