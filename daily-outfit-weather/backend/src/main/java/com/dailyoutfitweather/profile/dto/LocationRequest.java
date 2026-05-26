package com.dailyoutfitweather.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationRequest(
	@NotBlank @Size(max = 50) String sido,
	@NotBlank @Size(max = 50) String sigungu,
	@NotBlank @Size(max = 50) String dong,
	Integer nx,
	Integer ny
) {
}
