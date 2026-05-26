package com.dailyoutfitweather.recommendation.feedback.dto;

import com.dailyoutfitweather.recommendation.feedback.domain.RainFeedback;
import com.dailyoutfitweather.recommendation.feedback.domain.RecommendationFeedback;
import com.dailyoutfitweather.recommendation.feedback.domain.TemperatureFeedback;

public record FeedbackResponse(
	Long id,
	Long recommendationId,
	TemperatureFeedback temperatureFeedback,
	RainFeedback rainFeedback,
	String comment
) {
	public static FeedbackResponse from(RecommendationFeedback feedback) {
		return new FeedbackResponse(
			feedback.getId(),
			feedback.getRecommendationId(),
			feedback.getTemperatureFeedback(),
			feedback.getRainFeedback(),
			feedback.getComment()
		);
	}
}
