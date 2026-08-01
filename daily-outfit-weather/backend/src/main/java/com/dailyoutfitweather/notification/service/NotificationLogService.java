package com.dailyoutfitweather.notification.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailyoutfitweather.notification.domain.NotificationLog;
import com.dailyoutfitweather.notification.domain.NotificationType;
import com.dailyoutfitweather.notification.dto.NotificationLogResponse;
import com.dailyoutfitweather.notification.repository.NotificationLogRepository;
import com.dailyoutfitweather.recommendation.dto.RecommendationResponse;
import com.dailyoutfitweather.recommendation.service.RecommendationService;
import com.dailyoutfitweather.user.domain.ChangeAlertOption;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.UserProfileRepository;

@Service
public class NotificationLogService {

	private final UserProfileRepository userProfileRepository;
	private final RecommendationService recommendationService;
	private final NotificationLogRepository notificationLogRepository;
	private final Clock clock;

	public NotificationLogService(
		UserProfileRepository userProfileRepository,
		RecommendationService recommendationService,
		NotificationLogRepository notificationLogRepository,
		Clock clock
	) {
		this.userProfileRepository = userProfileRepository;
		this.recommendationService = recommendationService;
		this.notificationLogRepository = notificationLogRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<NotificationLogResponse> getLogs(User user) {
		return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
			.map(NotificationLogResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<NotificationLogResponse> getTodayLogs(User user) {
		LocalDate today = LocalDate.now(clock);
		Instant startInclusive = today.atStartOfDay(clock.getZone()).toInstant();
		Instant endExclusive = today.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
		return notificationLogRepository
			.findByUserIdAndScheduledAtBetweenOrderByScheduledAtDesc(user.getId(), startInclusive, endExclusive)
			.stream()
			.map(NotificationLogResponse::from)
			.toList();
	}

	@Transactional
	public List<NotificationLogResponse> generateDueLogs() {
		LocalDate targetDate = LocalDate.now(clock);
		LocalTime currentTime = LocalTime.now(clock);
		return generateDueLogs(targetDate, currentTime).stream()
			.map(NotificationLogResponse::from)
			.toList();
	}

	@Transactional
	public List<NotificationLog> generateDueLogs(LocalDate targetDate, LocalTime currentTime) {
		return userProfileRepository
			.findByNotificationTimeLessThanEqualAndChangeAlertOptionNot(currentTime, ChangeAlertOption.OFF)
			.stream()
			.map(profile -> createLogIfAbsent(profile, targetDate))
			.flatMap(List::stream)
			.toList();
	}

	@Transactional
	public int generateDueLogCount() {
		return generateDueLogs().size();
	}

	private List<NotificationLog> createLogIfAbsent(UserProfile profile, LocalDate targetDate) {
		User user = profile.getUser();
		Instant scheduledAt = scheduledAt(profile, targetDate);
		if (notificationLogRepository.existsByUserIdAndNotificationTypeAndScheduledAt(
			user.getId(),
			NotificationType.MORNING_REGULAR,
			scheduledAt
		)) {
			return List.of();
		}
		RecommendationResponse recommendation = recommendationService.getOrCreateRecommendation(user, targetDate);
		String title = "오늘 뭐입지?";
		String body = NotificationRecommendationSummary.body(recommendation);
		int inserted = notificationLogRepository.insertPendingLogIfAbsent(
			user.getId(),
			recommendation.id(),
			NotificationType.MORNING_REGULAR.name(),
			title,
			body,
			scheduledAt
		);
		if (inserted == 0) {
			return List.of();
		}
		return notificationLogRepository
			.findByUserIdAndNotificationTypeAndScheduledAt(user.getId(), NotificationType.MORNING_REGULAR, scheduledAt)
			.map(List::of)
			.orElseGet(List::of);
	}

	private Instant scheduledAt(UserProfile profile, LocalDate targetDate) {
		return LocalDateTime.of(targetDate, profile.getNotificationTime())
			.atZone(clock.getZone())
			.toInstant();
	}
}
