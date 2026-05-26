package com.dailyoutfitweather.notification.dto;

import java.time.Instant;

import com.dailyoutfitweather.notification.domain.NotificationLog;
import com.dailyoutfitweather.notification.domain.NotificationStatus;
import com.dailyoutfitweather.notification.domain.NotificationType;

public record NotificationLogResponse(
	Long id,
	Long recommendationId,
	NotificationType notificationType,
	String title,
	String body,
	Instant scheduledAt,
	Instant sentAt,
	NotificationStatus status,
	String failureReason,
	Instant createdAt
) {
	public static NotificationLogResponse from(NotificationLog notificationLog) {
		return new NotificationLogResponse(
			notificationLog.getId(),
			notificationLog.getRecommendationId(),
			notificationLog.getNotificationType(),
			notificationLog.getTitle(),
			notificationLog.getBody(),
			notificationLog.getScheduledAt(),
			notificationLog.getSentAt(),
			notificationLog.getStatus(),
			notificationLog.getFailureReason(),
			notificationLog.getCreatedAt()
		);
	}
}
