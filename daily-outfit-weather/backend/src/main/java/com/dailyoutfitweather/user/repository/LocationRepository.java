package com.dailyoutfitweather.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyoutfitweather.user.domain.Location;
import com.dailyoutfitweather.user.domain.LocationType;

public interface LocationRepository extends JpaRepository<Location, Long> {

	List<Location> findByUserId(Long userId);

	Optional<Location> findByUserIdAndType(Long userId, LocationType type);
}
