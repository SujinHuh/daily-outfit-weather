package com.dailyoutfitweather.profile.dto;

import com.dailyoutfitweather.user.domain.Location;

public record LocationResponse(
	String sido,
	String sigungu,
	String dong,
	Integer nx,
	Integer ny
) {

	public static LocationResponse from(Location location) {
		return new LocationResponse(
			location.getSido(),
			location.getSigungu(),
			location.getDong(),
			location.getNx(),
			location.getNy()
		);
	}
}
