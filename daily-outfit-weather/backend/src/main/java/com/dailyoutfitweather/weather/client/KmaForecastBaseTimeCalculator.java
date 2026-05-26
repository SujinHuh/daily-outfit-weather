package com.dailyoutfitweather.weather.client;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class KmaForecastBaseTimeCalculator {

	private static final List<LocalTime> BASE_TIMES = List.of(
		LocalTime.of(2, 0),
		LocalTime.of(5, 0),
		LocalTime.of(8, 0),
		LocalTime.of(11, 0),
		LocalTime.of(14, 0),
		LocalTime.of(17, 0),
		LocalTime.of(20, 0),
		LocalTime.of(23, 0)
	);

	public ForecastBaseTime latestBaseTime(Clock clock) {
		LocalDateTime availableDateTime = LocalDateTime.now(clock).minusMinutes(10);
		LocalDate date = availableDateTime.toLocalDate();
		LocalTime availableTime = availableDateTime.toLocalTime();
		for (int i = BASE_TIMES.size() - 1; i >= 0; i--) {
			LocalTime baseTime = BASE_TIMES.get(i);
			if (!availableTime.isBefore(baseTime)) {
				return new ForecastBaseTime(date, KmaForecastTime.format(baseTime));
			}
		}
		return new ForecastBaseTime(date.minusDays(1), "2300");
	}

	public ForecastBaseTime baseTimeForTarget(Clock clock, LocalDate targetDate, LocalTime targetTime) {
		LocalDateTime latestAvailable = toDateTime(latestBaseTime(clock));
		LocalDateTime forecastDateTime = LocalDateTime.of(targetDate, KmaForecastTime.targetForecastTime(targetTime));
		LocalDateTime latestUseful = forecastDateTime.minusHours(1);
		LocalDateTime cutoff = latestAvailable.isBefore(latestUseful) ? latestAvailable : latestUseful;

		for (int dayOffset = 0; dayOffset <= 1; dayOffset++) {
			LocalDate date = targetDate.minusDays(dayOffset);
			for (int i = BASE_TIMES.size() - 1; i >= 0; i--) {
				LocalDateTime candidate = LocalDateTime.of(date, BASE_TIMES.get(i));
				if (!candidate.isAfter(cutoff)) {
					return new ForecastBaseTime(date, KmaForecastTime.format(BASE_TIMES.get(i)));
				}
			}
		}
		return new ForecastBaseTime(targetDate.minusDays(1), "2300");
	}

	private LocalDateTime toDateTime(ForecastBaseTime baseTime) {
		int hour = Integer.parseInt(baseTime.time().substring(0, 2));
		return LocalDateTime.of(baseTime.date(), LocalTime.of(hour, 0));
	}
}
