package com.dailyoutfitweather.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.dailyoutfitweather.weather.client.ForecastBaseTime;
import com.dailyoutfitweather.weather.client.KmaForecastBaseTimeCalculator;

class KmaForecastBaseTimeCalculatorTest {

	private final KmaForecastBaseTimeCalculator calculator = new KmaForecastBaseTimeCalculator();

	@Test
	void usesLatestAvailableBaseTimeWithTenMinuteDelay() throws Exception {
		ForecastBaseTime baseTime = calculator.latestBaseTime(clock("2026-05-25T02:09:00+09:00"));

		assertThat(baseTime.date()).isEqualTo(java.time.LocalDate.of(2026, 5, 24));
		assertThat(baseTime.time()).isEqualTo("2300");
	}

	@Test
	void usesPreviousDayBaseTimeAroundMidnightDelay() {
		ForecastBaseTime baseTime = calculator.latestBaseTime(clock("2026-05-25T00:05:00+09:00"));

		assertThat(baseTime.date()).isEqualTo(java.time.LocalDate.of(2026, 5, 24));
		assertThat(baseTime.time()).isEqualTo("2300");
	}

	@Test
	void usesCurrentDateBaseTimeAfterDelay() throws Exception {
		ForecastBaseTime baseTime = calculator.latestBaseTime(clock("2026-05-25T08:15:00+09:00"));

		assertThat(baseTime.date()).isEqualTo(java.time.LocalDate.of(2026, 5, 25));
		assertThat(baseTime.time()).isEqualTo("0800");
	}

	@Test
	void choosesBaseTimeThatCanContainMorningTargetAfterMorningRelease() {
		ForecastBaseTime baseTime = calculator.baseTimeForTarget(
			clock("2026-05-25T14:10:00+09:00"),
			java.time.LocalDate.of(2026, 5, 25),
			java.time.LocalTime.of(8, 30)
		);

		assertThat(baseTime.date()).isEqualTo(java.time.LocalDate.of(2026, 5, 25));
		assertThat(baseTime.time()).isEqualTo("0800");
	}

	@Test
	void doesNotUseFutureBaseTimeForEveningTarget() {
		ForecastBaseTime baseTime = calculator.baseTimeForTarget(
			clock("2026-05-25T14:10:00+09:00"),
			java.time.LocalDate.of(2026, 5, 25),
			java.time.LocalTime.of(18, 30)
		);

		assertThat(baseTime.date()).isEqualTo(java.time.LocalDate.of(2026, 5, 25));
		assertThat(baseTime.time()).isEqualTo("1400");
	}

	private Clock clock(String instant) {
		return Clock.fixed(Instant.from(java.time.OffsetDateTime.parse(instant)), ZoneId.of("Asia/Seoul"));
	}

}
