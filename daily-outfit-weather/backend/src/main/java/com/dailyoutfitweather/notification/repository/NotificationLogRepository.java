package com.dailyoutfitweather.notification.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyoutfitweather.notification.domain.NotificationLog;
import com.dailyoutfitweather.notification.domain.NotificationType;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

	boolean existsByUserIdAndNotificationTypeAndScheduledAt(Long userId, NotificationType notificationType, Instant scheduledAt);

	List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<NotificationLog> findByUserIdAndScheduledAtBetweenOrderByScheduledAtDesc(
		Long userId,
		Instant startInclusive,
		Instant endExclusive
	);
}
