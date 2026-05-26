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
import com.dailyoutfitweather.recommendation.domain.OutfitRecommendation;
import com.dailyoutfitweather.recommendation.repository.OutfitRecommendationRepository;
import com.dailyoutfitweather.user.domain.ChangeAlertOption;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.domain.UserProfile;
import com.dailyoutfitweather.user.repository.UserProfileRepository;

@Service
public class NotificationLogService {

	private final UserProfileRepository userProfileRepository;
	private final OutfitRecommendationRepository outfitRecommendationRepository;
	private final NotificationLogRepository notificationLogRepository;
	private final Clock clock;

	public NotificationLogService(
		UserProfileRepository userProfileRepository,
		OutfitRecommendationRepository outfitRecommendationRepository,
		NotificationLogRepository notificationLogRepository,
		Clock clock
	) {
		this.userProfileRepository = userProfileRepository;
		this.outfitRecommendationRepository = outfitRecommendationRepository;
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
		OutfitRecommendation recommendation = outfitRecommendationRepository
			.findByUserIdAndTargetDate(user.getId(), targetDate)
			.orElse(null);
		String title = "오늘 뭐입지?";
		String body = recommendation == null
			? "오늘 추천을 확인할 시간입니다."
			: recommendation.getSummaryMessage();
		Instant scheduledAt = scheduledAt(profile, targetDate);
		int inserted = notificationLogRepository.insertPendingLogIfAbsent(
			user.getId(),
			recommendation == null ? null : recommendation.getId(),
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
