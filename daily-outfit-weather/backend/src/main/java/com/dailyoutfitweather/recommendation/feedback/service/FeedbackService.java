package com.dailyoutfitweather.recommendation.feedback.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;
import com.dailyoutfitweather.recommendation.feedback.domain.RecommendationFeedback;
import com.dailyoutfitweather.recommendation.feedback.dto.FeedbackRequest;
import com.dailyoutfitweather.recommendation.feedback.dto.FeedbackResponse;
import com.dailyoutfitweather.recommendation.feedback.repository.RecommendationFeedbackRepository;
import com.dailyoutfitweather.recommendation.repository.OutfitRecommendationRepository;
import com.dailyoutfitweather.user.domain.User;

@Service
public class FeedbackService {

	private final OutfitRecommendationRepository outfitRecommendationRepository;
	private final RecommendationFeedbackRepository feedbackRepository;

	public FeedbackService(
		OutfitRecommendationRepository outfitRecommendationRepository,
		RecommendationFeedbackRepository feedbackRepository
	) {
		this.outfitRecommendationRepository = outfitRecommendationRepository;
		this.feedbackRepository = feedbackRepository;
	}

	@Transactional
	public FeedbackResponse saveFeedback(User user, Long recommendationId, FeedbackRequest request) {
		if (request.temperatureFeedback() == null && request.rainFeedback() == null && isBlank(request.comment())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "피드백 항목을 하나 이상 입력해야 합니다.");
		}

		OutfitRecommendation recommendation = outfitRecommendationRepository.findByIdAndUserId(recommendationId, user.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천을 찾을 수 없습니다."));

		RecommendationFeedback feedback = feedbackRepository.findByRecommendation_IdAndUser_Id(recommendationId, user.getId())
			.orElseGet(() -> new RecommendationFeedback(
				recommendation,
				user,
				request.temperatureFeedback(),
				request.rainFeedback(),
				normalizeComment(request.comment())
			));
		feedback.update(request.temperatureFeedback(), request.rainFeedback(), normalizeComment(request.comment()));
		return FeedbackResponse.from(feedbackRepository.save(feedback));
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String normalizeComment(String comment) {
		return isBlank(comment) ? null : comment.strip();
	}
}
