package com.dailyoutfitweather.profile.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dailyoutfitweather.global.security.LoginUser;
import com.dailyoutfitweather.profile.dto.ProfileRequest;
import com.dailyoutfitweather.profile.dto.ProfileResponse;
import com.dailyoutfitweather.profile.service.ProfileService;
import com.dailyoutfitweather.user.domain.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@PostMapping("/onboarding")
	ProfileResponse saveOnboarding(@LoginUser User user, @Valid @RequestBody ProfileRequest request) {
		return profileService.saveOnboarding(user, request);
	}

	@GetMapping
	ProfileResponse getProfile(@LoginUser User user) {
		return profileService.getProfile(user);
	}

	@PutMapping
	ProfileResponse updateProfile(@LoginUser User user, @Valid @RequestBody ProfileRequest request) {
		return profileService.updateProfile(user, request);
	}
}
