package com.dailyoutfitweather.recommendation.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;

public interface OutfitRecommendationRepository extends JpaRepository<OutfitRecommendation, Long> {

	Optional<OutfitRecommendation> findByUserIdAndTargetDate(Long userId, LocalDate targetDate);

	Optional<OutfitRecommendation> findByIdAndUserId(Long id, Long userId);
}
