package com.dailyoutfitweather.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.recommendation.service.WeatherSnapshotProvider;
import com.dailyoutfitweather.recommendation.service.WeatherSnapshotProvider.WeatherSnapshots;
import com.dailyoutfitweather.user.domain.AuthProvider;
import com.dailyoutfitweather.user.domain.ChangeAlertOption;
import com.dailyoutfitweather.user.domain.Location;
import com.dailyoutfitweather.user.domain.LocationType;
import com.dailyoutfitweather.user.domain.MessageTone;
import com.dailyoutfitweather.user.domain.TransportType;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.LocationRepository;
import com.dailyoutfitweather.weather.client.WeatherApiClient;
import com.dailyoutfitweather.weather.client.WeatherApiException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeatherSnapshotProviderTest {

	@Mock
	private LocationRepository locationRepository;

	@Mock
	private WeatherApiClient weatherApiClient;

	private final Clock clock = Clock.fixed(Instant.parse("2026-05-24T15:30:00Z"), ZoneId.of("Asia/Seoul"));

	private WeatherSnapshotProvider provider;
	private User user;
	private UserProfile profile;

	@BeforeEach
	void setUp() {
		provider = new WeatherSnapshotProvider(locationRepository, weatherApiClient, clock, true);
		user = new User("dev-user@daily-outfit-weather.local", "수진", AuthProvider.DEV, "dev-user");
		profile = new UserProfile(user);
		profile.update(
			4,
			2,
			LocalTime.of(8, 30),
			LocalTime.of(18, 30),
			LocalTime.of(7, 30),
			TransportType.PUBLIC_TRANSPORT,
			MessageTone.CHARACTER,
			ChangeAlertOption.IMPORTANT_ONLY
		);
	}

	@Test
	void throwsWhenFallbackIsDisabledAndGridCoordinateIsMissing() {
		WeatherSnapshotProvider strictProvider = new WeatherSnapshotProvider(locationRepository, weatherApiClient, clock, false);
		when(locationRepository.findByUserIdAndType(user.getId(), LocationType.HOME))
			.thenReturn(Optional.of(new Location(user, LocationType.HOME, "서울특별시", "강남구", "역삼동", null, null)));

		assertThatThrownBy(() -> strictProvider.getTodayWeather(user, profile))
			.isInstanceOf(WeatherApiException.class)
			.hasMessageContaining("Location grid is missing");
	}

	@Test
	void fallsBackWhenGridCoordinateIsMissing() {
		when(locationRepository.findByUserIdAndType(user.getId(), LocationType.HOME))
			.thenReturn(Optional.of(new Location(user, LocationType.HOME, "서울특별시", "강남구", "역삼동", null, null)));

		WeatherSnapshots snapshots = provider.getTodayWeather(user, profile);

		assertThat(snapshots.commuteWeather().temperature()).isEqualTo(17);
		assertThat(snapshots.leaveWorkWeather().temperature()).isEqualTo(14);
		Mockito.verifyNoInteractions(weatherApiClient);
	}

	@Test
	void fallsBackOnlyForFailedLocationWhenWeatherApiFails() {
		Location home = new Location(user, LocationType.HOME, "서울특별시", "강남구", "역삼동", 61, 125);
		Location work = new Location(user, LocationType.WORK, "서울특별시", "성동구", "성수동", 62, 126);
		when(locationRepository.findByUserIdAndType(user.getId(), LocationType.HOME)).thenReturn(Optional.of(home));
		when(locationRepository.findByUserIdAndType(user.getId(), LocationType.WORK)).thenReturn(Optional.of(work));
		WeatherSnapshot leaveWork = new WeatherSnapshot(3, 1, 0, PrecipitationType.NONE, 4.1);
		when(weatherApiClient.getForecast(Mockito.eq(61), Mockito.eq(125), Mockito.any(), Mockito.any()))
			.thenThrow(new WeatherApiException("failed"));
		when(weatherApiClient.getForecast(Mockito.eq(62), Mockito.eq(126), Mockito.any(), Mockito.any()))
			.thenReturn(leaveWork);

		WeatherSnapshots snapshots = provider.getTodayWeather(user, profile);

		assertThat(snapshots.commuteWeather().temperature()).isEqualTo(17);
		assertThat(snapshots.leaveWorkWeather()).isEqualTo(leaveWork);
	}

	@Test
	void rejectsNullUserAndProfileBeforeFallback() {
		assertThatNullPointerException()
			.isThrownBy(() -> provider.getTodayWeather(null, profile))
			.withMessage("user must not be null");

		assertThatNullPointerException()
			.isThrownBy(() -> provider.getTodayWeather(user, null))
			.withMessage("profile must not be null");
	}

	@Test
	void usesHomeForCommuteAndWorkForLeaveWorkWithSameTargetDate() {
		Location home = new Location(user, LocationType.HOME, "서울특별시", "강남구", "역삼동", 61, 125);
		Location work = new Location(user, LocationType.WORK, "서울특별시", "성동구", "성수동", 62, 126);
		WeatherSnapshot commute = new WeatherSnapshot(18, 17, 10, PrecipitationType.NONE, 1.0);
		WeatherSnapshot leaveWork = new WeatherSnapshot(13, 12, 80, PrecipitationType.RAIN, 3.0);
		when(locationRepository.findByUserIdAndType(user.getId(), LocationType.HOME)).thenReturn(Optional.of(home));
		when(locationRepository.findByUserIdAndType(user.getId(), LocationType.WORK)).thenReturn(Optional.of(work));
		when(weatherApiClient.getForecast(Mockito.eq(61), Mockito.eq(125), Mockito.eq(java.time.LocalDate.of(2026, 5, 25)), Mockito.eq(LocalTime.of(8, 30))))
			.thenReturn(commute);
		when(weatherApiClient.getForecast(Mockito.eq(62), Mockito.eq(126), Mockito.eq(java.time.LocalDate.of(2026, 5, 25)), Mockito.eq(LocalTime.of(18, 30))))
			.thenReturn(leaveWork);

		WeatherSnapshots snapshots = provider.getTodayWeather(user, profile);

		assertThat(snapshots.commuteWeather()).isEqualTo(commute);
		assertThat(snapshots.leaveWorkWeather()).isEqualTo(leaveWork);
	}
}
