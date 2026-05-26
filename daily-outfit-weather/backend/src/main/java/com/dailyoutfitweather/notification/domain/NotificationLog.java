package com.dailyoutfitweather.notification.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "notification_logs", uniqueConstraints = {
	@UniqueConstraint(
		name = "uk_notification_logs_user_type_scheduled_at",
		columnNames = {"user_id", "notification_type", "scheduled_at"}
	)
})
public class NotificationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recommendation_id")
	private OutfitRecommendation recommendation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private NotificationType notificationType;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String body;

	@Column(nullable = false)
	private Instant scheduledAt;

	private Instant sentAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private NotificationStatus status;

	@Column(columnDefinition = "text")
	private String failureReason;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected NotificationLog() {
	}

	public NotificationLog(
		User user,
		OutfitRecommendation recommendation,
		NotificationType notificationType,
		String title,
		String body,
		Instant scheduledAt
	) {
		this.user = user;
		this.recommendation = recommendation;
		this.notificationType = notificationType;
		this.title = title;
		this.body = body;
		this.scheduledAt = scheduledAt;
		this.status = NotificationStatus.PENDING;
	}

	public Long getId() {
		return id;
	}

	public Long getRecommendationId() {
		return recommendation == null ? null : recommendation.getId();
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public Instant getScheduledAt() {
		return scheduledAt;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
