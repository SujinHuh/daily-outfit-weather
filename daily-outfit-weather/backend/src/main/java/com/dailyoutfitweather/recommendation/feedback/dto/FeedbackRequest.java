package com.dailyoutfitweather.recommendation.feedback.dto;

import com.dailyoutfitweather.recommendation.feedback.domain.RainFeedback;
import com.dailyoutfitweather.recommendation.feedback.domain.TemperatureFeedback;

import jakarta.validation.constraints.Size;

public record FeedbackRequest(
	TemperatureFeedback temperatureFeedback,
	RainFeedback rainFeedback,
	@Size(max = 500) String comment
) {
}
