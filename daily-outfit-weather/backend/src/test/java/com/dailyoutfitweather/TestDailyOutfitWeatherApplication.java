package com.dailyoutfitweather;

import org.springframework.boot.SpringApplication;

import com.dailyoutfitweather.support.TestcontainersConfiguration;

class TestDailyOutfitWeatherApplication {

	public static void main(String[] args) {
		SpringApplication.from(DailyOutfitWeatherApplication::main)
			.with(TestcontainersConfiguration.class)
			.run(args);
	}
}
