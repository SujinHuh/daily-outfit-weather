package com.dailyoutfitweather.recommendation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.user.domain.Location;
import com.dailyoutfitweather.user.domain.LocationType;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.LocationRepository;
import com.dailyoutfitweather.weather.client.WeatherApiClient;
import com.dailyoutfitweather.weather.client.WeatherApiException;

@Component
public class WeatherSnapshotProvider {

	private static final Logger log = LoggerFactory.getLogger(WeatherSnapshotProvider.class);

	private final LocationRepository locationRepository;
	private final WeatherApiClient weatherApiClient;
	private final Clock clock;
	private final boolean fallbackEnabled;

	public WeatherSnapshotProvider(
		LocationRepository locationRepository,
		WeatherApiClient weatherApiClient,
		Clock clock,
		@Value("${app.weather.fallback-enabled:true}") boolean fallbackEnabled
	) {
		this.locationRepository = locationRepository;
		this.weatherApiClient = weatherApiClient;
		this.clock = clock;
		this.fallbackEnabled = fallbackEnabled;
	}

	public WeatherSnapshots getTodayWeather(User user, UserProfile profile) {
		Objects.requireNonNull(user, "user must not be null");
		Objects.requireNonNull(profile, "profile must not be null");
		LocalDate targetDate = LocalDate.now(clock);
		return new WeatherSnapshots(
			forecastOrDefault(user, LocationType.HOME, targetDate, profile.getCommuteTime(), defaultCommuteWeather()),
			forecastOrDefault(user, LocationType.WORK, targetDate, profile.getLeaveWorkTime(), defaultLeaveWorkWeather())
		);
	}

	private WeatherSnapshot forecastOrDefault(
		User user,
		LocationType locationType,
		LocalDate targetDate,
		java.time.LocalTime targetTime,
		WeatherSnapshot defaultWeather
	) {
		try {
			Location location = requiredGridLocation(user, locationType);
			return weatherApiClient.getForecast(location.getNx(), location.getNy(), targetDate, targetTime);
		}
		catch (WeatherApiException exception) {
			if (!fallbackEnabled) {
				throw exception;
			}
			log.warn("Falling back to default weather for locationType={} targetDate={} targetTime={}: {}",
				locationType, targetDate, targetTime, exception.getMessage());
			return defaultWeather;
		}
	}

	private Location requiredGridLocation(User user, LocationType type) {
		Location location = locationRepository.findByUserIdAndType(user.getId(), type)
			.orElseThrow(() -> new WeatherApiException("Location is missing: " + type));
		if (location.getNx() == null || location.getNy() == null) {
			throw new WeatherApiException("Location grid is missing: " + type);
		}
		return location;
	}

	private WeatherSnapshot defaultCommuteWeather() {
		return new WeatherSnapshot(17, 16, 20, PrecipitationType.NONE, 2.4, 55);
	}

	private WeatherSnapshot defaultLeaveWorkWeather() {
		return new WeatherSnapshot(14, 13, 30, PrecipitationType.NONE, 2.8, 60);
	}

	public record WeatherSnapshots(WeatherSnapshot commuteWeather, WeatherSnapshot leaveWorkWeather) {
	}
}
