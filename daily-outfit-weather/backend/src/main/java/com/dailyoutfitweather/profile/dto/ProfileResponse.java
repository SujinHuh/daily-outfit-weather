package com.dailyoutfitweather.profile.dto;

import java.time.LocalTime;

import com.dailyoutfitweather.user.domain.ChangeAlertOption;
import com.dailyoutfitweather.user.domain.MessageTone;
import com.dailyoutfitweather.user.domain.TransportType;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;

public record ProfileResponse(
	Long userId,
	String email,
	String nickname,
	int coldSensitivity,
	int heatSensitivity,
	LocalTime commuteTime,
	LocalTime leaveWorkTime,
	LocalTime notificationTime,
	TransportType transportType,
	MessageTone messageTone,
	ChangeAlertOption changeAlertOption,
	LocationResponse homeLocation,
	LocationResponse workLocation
) {

	public static ProfileResponse of(
		User user,
		UserProfile profile,
		LocationResponse homeLocation,
		LocationResponse workLocation
	) {
		return new ProfileResponse(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			profile.getColdSensitivity(),
			profile.getHeatSensitivity(),
			profile.getCommuteTime(),
			profile.getLeaveWorkTime(),
			profile.getNotificationTime(),
			profile.getTransportType(),
			profile.getMessageTone(),
			profile.getChangeAlertOption(),
			homeLocation,
			workLocation
		);
	}
}
