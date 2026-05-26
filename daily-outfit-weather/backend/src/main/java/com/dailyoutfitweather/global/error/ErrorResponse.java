package com.dailyoutfitweather.global.error;

public record ErrorResponse(
	String code,
	String message
) {
}
