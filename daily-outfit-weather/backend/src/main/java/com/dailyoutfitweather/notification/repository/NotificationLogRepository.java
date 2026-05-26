package com.dailyoutfitweather.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dailyoutfitweather.notification.domain.NotificationLog;
import com.dailyoutfitweather.notification.domain.NotificationType;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

	boolean existsByUserIdAndNotificationTypeAndScheduledAt(Long userId, NotificationType notificationType, Instant scheduledAt);

	Optional<NotificationLog> findByUserIdAndNotificationTypeAndScheduledAt(
		Long userId,
		NotificationType notificationType,
		Instant scheduledAt
	);

	List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<NotificationLog> findByUserIdAndScheduledAtBetweenOrderByScheduledAtDesc(
		Long userId,
		Instant startInclusive,
		Instant endExclusive
	);

	@Modifying
	@Query(value = """
		insert into notification_logs (
		  user_id, recommendation_id, notification_type, title, body, scheduled_at, status, created_at
		) values (
		  :userId, :recommendationId, :notificationType, :title, :body, :scheduledAt, 'PENDING', now()
		)
		on conflict (user_id, notification_type, scheduled_at) do nothing
		""", nativeQuery = true)
	int insertPendingLogIfAbsent(
		@Param("userId") Long userId,
		@Param("recommendationId") Long recommendationId,
		@Param("notificationType") String notificationType,
		@Param("title") String title,
		@Param("body") String body,
		@Param("scheduledAt") Instant scheduledAt
	);
}
