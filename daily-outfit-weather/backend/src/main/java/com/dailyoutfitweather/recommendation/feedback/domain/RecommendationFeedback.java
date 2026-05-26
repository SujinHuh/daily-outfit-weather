package com.dailyoutfitweather.recommendation.feedback.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_feedbacks")
public class RecommendationFeedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recommendation_id", nullable = false, unique = true)
	private OutfitRecommendation recommendation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TemperatureFeedback temperatureFeedback;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private RainFeedback rainFeedback;

	@Column(columnDefinition = "text")
	private String comment;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected RecommendationFeedback() {
	}

	public RecommendationFeedback(
		OutfitRecommendation recommendation,
		User user,
		TemperatureFeedback temperatureFeedback,
		RainFeedback rainFeedback,
		String comment
	) {
		this.recommendation = recommendation;
		this.user = user;
		update(temperatureFeedback, rainFeedback, comment);
	}

	public void update(TemperatureFeedback temperatureFeedback, RainFeedback rainFeedback, String comment) {
		this.temperatureFeedback = temperatureFeedback;
		this.rainFeedback = rainFeedback;
		this.comment = comment;
	}

	public Long getId() {
		return id;
	}

	public Long getRecommendationId() {
		return recommendation.getId();
	}

	public TemperatureFeedback getTemperatureFeedback() {
		return temperatureFeedback;
	}

	public RainFeedback getRainFeedback() {
		return rainFeedback;
	}

	public String getComment() {
		return comment;
	}
}
