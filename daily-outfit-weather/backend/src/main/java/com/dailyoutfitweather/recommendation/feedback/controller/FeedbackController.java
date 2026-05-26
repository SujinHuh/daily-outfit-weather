package com.dailyoutfitweather.recommendation.feedback.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dailyoutfitweather.global.security.LoginUser;
import com.dailyoutfitweather.recommendation.feedback.dto.FeedbackRequest;
import com.dailyoutfitweather.recommendation.feedback.dto.FeedbackResponse;
import com.dailyoutfitweather.recommendation.feedback.service.FeedbackService;
import com.dailyoutfitweather.user.domain.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recommendations/{recommendationId}/feedback")
public class FeedbackController {

	private final FeedbackService feedbackService;

	public FeedbackController(FeedbackService feedbackService) {
		this.feedbackService = feedbackService;
	}

	@PostMapping
	FeedbackResponse saveFeedback(
		@LoginUser User user,
		@PathVariable Long recommendationId,
		@Valid @RequestBody FeedbackRequest request
	) {
		return feedbackService.saveFeedback(user, recommendationId, request);
	}
}
