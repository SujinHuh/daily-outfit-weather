package com.dailyoutfitweather.profile.dto;

import java.time.LocalTime;

import com.dailyoutfitweather.user.domain.ChangeAlertOption;
import com.dailyoutfitweather.user.domain.MessageTone;
import com.dailyoutfitweather.user.domain.TransportType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
	@NotBlank @Size(max = 50) String nickname,
	@Min(1) @Max(5) int coldSensitivity,
	@Min(1) @Max(5) int heatSensitivity,
	@NotNull LocalTime commuteTime,
	@NotNull LocalTime leaveWorkTime,
	@NotNull LocalTime notificationTime,
	@NotNull TransportType transportType,
	@NotNull MessageTone messageTone,
	@NotNull ChangeAlertOption changeAlertOption,
	@Valid @NotNull LocationRequest homeLocation,
	@Valid @NotNull LocationRequest workLocation
) {
}
