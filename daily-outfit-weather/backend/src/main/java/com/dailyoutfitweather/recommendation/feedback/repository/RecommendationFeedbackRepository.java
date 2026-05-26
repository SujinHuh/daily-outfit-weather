package com.dailyoutfitweather.recommendation.feedback.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyoutfitweather.recommendation.feedback.domain.RecommendationFeedback;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {

	Optional<RecommendationFeedback> findByRecommendation_IdAndUser_Id(Long recommendationId, Long userId);
}
