package com.dailyoutfitweather.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.dailyoutfitweather.notification.domain.NotificationLog;
import com.dailyoutfitweather.notification.domain.NotificationStatus;
import com.dailyoutfitweather.notification.domain.NotificationType;
import com.dailyoutfitweather.notification.service.NotificationLogService;
import com.dailyoutfitweather.support.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NotificationLogServiceTest {

	@Autowired
	private NotificationLogService notificationLogService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("delete from recommendation_feedbacks");
		jdbcTemplate.update("delete from notification_logs");
		jdbcTemplate.update("delete from outfit_recommendations");
		jdbcTemplate.update("delete from locations");
		jdbcTemplate.update("delete from user_profiles");
		jdbcTemplate.update("delete from users");
	}

	@Test
	void generateDueLogsForProfilesAtOrBeforeCurrentTime() {
		Long dueUserId = insertUserWithProfile("due@example.com", "07:30", "IMPORTANT_ONLY");
		insertUserWithProfile("future@example.com", "08:30", "IMPORTANT_ONLY");
		insertUserWithProfile("off@example.com", "07:00", "OFF");

		List<NotificationLog> logs = notificationLogService.generateDueLogs(
			LocalDate.of(2026, 5, 25),
			LocalTime.of(8, 0)
		);

		assertThat(logs).hasSize(1);
		NotificationLog log = logs.get(0);
		assertThat(log.getNotificationType()).isEqualTo(NotificationType.MORNING_REGULAR);
		assertThat(log.getStatus()).isEqualTo(NotificationStatus.PENDING);
		assertThat(log.getRecommendationId()).isNotNull();
		assertThat(log.getBody())
			.contains("출근 체감 16도")
			.contains("퇴근 체감 13도")
			.contains("강수확률 30%")
			.doesNotContain("\n");
		assertThat(log.getBody()).isNotEqualTo("오늘 추천을 확인할 시간입니다.");

		Long savedUserId = jdbcTemplate.queryForObject("select user_id from notification_logs", Long.class);
		assertThat(savedUserId).isEqualTo(dueUserId);
		Long recommendationCount = jdbcTemplate.queryForObject("select count(*) from outfit_recommendations", Long.class);
		assertThat(recommendationCount).isEqualTo(1);
	}

	@Test
	void generateDueLogsDoesNotCreateDuplicatesForSameSchedule() {
		insertUserWithProfile("due@example.com", "07:30", "IMPORTANT_ONLY");
		LocalDate targetDate = LocalDate.of(2026, 5, 25);
		LocalTime currentTime = LocalTime.of(8, 0);

		notificationLogService.generateDueLogs(targetDate, currentTime);
		List<NotificationLog> second = notificationLogService.generateDueLogs(targetDate, currentTime);

		assertThat(second).isEmpty();
		Long count = jdbcTemplate.queryForObject("select count(*) from notification_logs", Long.class);
		assertThat(count).isEqualTo(1);
	}

	private Long insertUserWithProfile(String email, String notificationTime, String changeAlertOption) {
		Long userId = jdbcTemplate.queryForObject(
			"insert into users (email, nickname, provider, provider_id, created_at, updated_at) values (?, ?, ?, ?, now(), now()) returning id",
			Long.class,
			email,
			"수진",
			"GOOGLE",
			email
		);
		jdbcTemplate.update("""
			insert into user_profiles (
			  user_id, cold_sensitivity, heat_sensitivity, commute_time, leave_work_time,
			  notification_time, transport_type, message_tone, change_alert_option, created_at, updated_at
			) values (?, 3, 3, '08:30', '18:30', ?::time, 'PUBLIC_TRANSPORT', 'FRIENDLY', ?, now(), now())
			""", userId, notificationTime, changeAlertOption);
		return userId;
	}
}
