package com.dailyoutfitweather;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.dailyoutfitweather.support.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DailyOutfitWeatherApplicationTests {

	@Test
	void contextLoads() {
	}
}
