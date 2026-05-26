package com.dailyoutfitweather.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dailyoutfitweather.global.security.LoginUser;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.dto.UserResponse;

@RestController
@RequestMapping("/api")
public class AuthController {

	@GetMapping("/me")
	public UserResponse me(@LoginUser User user) {
		return UserResponse.from(user);
	}
}
