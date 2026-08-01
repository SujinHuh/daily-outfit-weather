package com.dailyoutfitweather.recommendation.domain;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.dailyoutfitweather.recommendation.dto.RecommendationResult;
import com.dailyoutfitweather.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "outfit_recommendations", uniqueConstraints = {
	@UniqueConstraint(name = "uk_outfit_recommendations_user_date", columnNames = {"user_id", "target_date"})
})
public class OutfitRecommendation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private LocalDate targetDate;

	@Column(nullable = false, columnDefinition = "text")
	private String summaryMessage;

	private String topRecommendation;

	private String outerRecommendation;

	private String itemRecommendation;

	@Column(length = 100)
	private String characterImageType;

	@Column(columnDefinition = "text")
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private RecommendationType recommendationType;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private RecommendationWeatherSnapshot weatherSnapshot;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected OutfitRecommendation() {
	}

	public OutfitRecommendation(
		User user,
		LocalDate targetDate,
		RecommendationResult result,
		RecommendationWeatherSnapshot weatherSnapshot
	) {
		this.user = user;
		this.targetDate = targetDate;
		this.summaryMessage = result.summaryMessage();
		this.topRecommendation = result.topRecommendation();
		this.outerRecommendation = result.outerRecommendation();
		this.itemRecommendation = result.itemRecommendation();
		this.characterImageType = result.characterImageType();
		this.reason = result.reason();
		this.recommendationType = RecommendationType.DAILY;
		this.weatherSnapshot = weatherSnapshot;
	}

	public void refresh(RecommendationResult result, RecommendationWeatherSnapshot weatherSnapshot) {
		this.summaryMessage = result.summaryMessage();
		this.topRecommendation = result.topRecommendation();
		this.outerRecommendation = result.outerRecommendation();
		this.itemRecommendation = result.itemRecommendation();
		this.characterImageType = result.characterImageType();
		this.reason = result.reason();
		this.weatherSnapshot = weatherSnapshot;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public String getSummaryMessage() {
		return summaryMessage;
	}

	public String getTopRecommendation() {
		return topRecommendation;
	}

	public String getOuterRecommendation() {
		return outerRecommendation;
	}

	public String getItemRecommendation() {
		return itemRecommendation;
	}

	public String getCharacterImageType() {
		return characterImageType;
	}

	public String getReason() {
		return reason;
	}

	public RecommendationWeatherSnapshot getWeatherSnapshot() {
		return weatherSnapshot;
	}
}
