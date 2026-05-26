package com.dailyoutfitweather.user.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyoutfitweather.user.domain.ChangeAlertOption;
import com.dailyoutfitweather.user.domain.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

	Optional<UserProfile> findByUserId(Long userId);

	List<UserProfile> findByNotificationTimeLessThanEqualAndChangeAlertOptionNot(
		LocalTime notificationTime,
		ChangeAlertOption changeAlertOption
	);
}
