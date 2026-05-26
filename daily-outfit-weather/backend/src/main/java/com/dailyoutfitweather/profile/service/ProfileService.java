package com.dailyoutfitweather.profile.service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailyoutfitweather.location.LocationGrid;
import com.dailyoutfitweather.location.LocationGridCatalog;
import com.dailyoutfitweather.profile.dto.LocationRequest;
import com.dailyoutfitweather.profile.dto.LocationResponse;
import com.dailyoutfitweather.profile.dto.ProfileRequest;
import com.dailyoutfitweather.profile.dto.ProfileResponse;
import com.dailyoutfitweather.user.domain.AuthProvider;
import com.dailyoutfitweather.user.domain.Location;
import com.dailyoutfitweather.user.domain.LocationType;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.LocationRepository;
import com.dailyoutfitweather.user.repository.UserProfileRepository;
import com.dailyoutfitweather.user.repository.UserRepository;

@Service
public class ProfileService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final LocationRepository locationRepository;
	private final LocationGridCatalog locationGridCatalog;

	public ProfileService(
		UserRepository userRepository,
		UserProfileRepository userProfileRepository,
		LocationRepository locationRepository,
		LocationGridCatalog locationGridCatalog
	) {
		this.userRepository = userRepository;
		this.userProfileRepository = userProfileRepository;
		this.locationRepository = locationRepository;
		this.locationGridCatalog = locationGridCatalog;
	}

	@Transactional
	public ProfileResponse saveOnboarding(User user, ProfileRequest request) {
		user.updateNickname(request.nickname());
		userRepository.save(user);
		return saveProfile(user, request);
	}

	@Transactional
	public ProfileResponse updateProfile(User user, ProfileRequest request) {
		if (userProfileRepository.findByUserId(user.getId()).isEmpty()) {
			throw new ProfileNotFoundException();
		}
		user.updateNickname(request.nickname());
		userRepository.save(user);
		return saveProfile(user, request);
	}

	private ProfileResponse saveProfile(User user, ProfileRequest request) {
		UserProfile profile = userProfileRepository.findByUserId(user.getId())
			.orElseGet(() -> new UserProfile(user));
		profile.update(
			request.coldSensitivity(),
			request.heatSensitivity(),
			request.commuteTime(),
			request.leaveWorkTime(),
			request.notificationTime(),
			request.transportType(),
			request.messageTone(),
			request.changeAlertOption()
		);
		userProfileRepository.save(profile);

		Location homeLocation = upsertLocation(user, LocationType.HOME, request.homeLocation());
		Location workLocation = upsertLocation(user, LocationType.WORK, request.workLocation());

		return ProfileResponse.of(user, profile, LocationResponse.from(homeLocation), LocationResponse.from(workLocation));
	}

	@Transactional(readOnly = true)
	public ProfileResponse getProfile(User user) {
		UserProfile profile = userProfileRepository.findByUserId(user.getId())
			.orElseThrow(ProfileNotFoundException::new);
		Map<LocationType, Location> locations = locationRepository.findByUserId(user.getId()).stream()
			.collect(Collectors.toMap(Location::getType, Function.identity()));

		return ProfileResponse.of(
			user,
			profile,
			LocationResponse.from(requiredLocation(locations, LocationType.HOME)),
			LocationResponse.from(requiredLocation(locations, LocationType.WORK))
		);
	}

	private Location upsertLocation(User user, LocationType type, LocationRequest request) {
		LocationGridCoordinate coordinate = resolveGridCoordinate(request);
		Location location = locationRepository.findByUserIdAndType(user.getId(), type)
			.orElseGet(() -> new Location(
				user,
				type,
				request.sido(),
				request.sigungu(),
				request.dong(),
				coordinate.nx(),
				coordinate.ny()
			));
		location.update(request.sido(), request.sigungu(), request.dong(), coordinate.nx(), coordinate.ny());
		return locationRepository.save(location);
	}

	private LocationGridCoordinate resolveGridCoordinate(LocationRequest request) {
		if (request.nx() != null && request.ny() != null) {
			return new LocationGridCoordinate(request.nx(), request.ny());
		}
		return locationGridCatalog.find(request.sido(), request.sigungu(), request.dong())
			.map(grid -> new LocationGridCoordinate(grid.nx(), grid.ny()))
			.orElseGet(() -> new LocationGridCoordinate(request.nx(), request.ny()));
	}

	private record LocationGridCoordinate(Integer nx, Integer ny) {
	}

	private Location requiredLocation(Map<LocationType, Location> locations, LocationType type) {
		Location location = locations.get(type);
		if (location == null) {
			throw new ProfileNotFoundException();
		}
		return location;
	}
}
