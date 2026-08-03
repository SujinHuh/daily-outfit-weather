package com.dailyoutfitweather.recommendation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dailyoutfitweather.global.security.LoginUser;
import com.dailyoutfitweather.recommendation.dto.RecommendationResponse;
import com.dailyoutfitweather.recommendation.dto.WeeklyRecommendationResponse;
import com.dailyoutfitweather.recommendation.service.RecommendationService;
import com.dailyoutfitweather.user.domain.User;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

	private final RecommendationService recommendationService;

	public RecommendationController(RecommendationService recommendationService) {
		this.recommendationService = recommendationService;
	}

	@GetMapping("/today")
	RecommendationResponse getTodayRecommendation(@LoginUser User user) {
		return recommendationService.getOrCreateTodayRecommendation(user);
	}

	@PostMapping("/today")
	RecommendationResponse createTodayRecommendation(@LoginUser User user) {
		return recommendationService.getOrCreateTodayRecommendation(user);
	}

	@GetMapping("/weekly")
	WeeklyRecommendationResponse getWeeklyRecommendation(@LoginUser User user) {
		return recommendationService.getWeeklyRecommendation(user);
	}
}
