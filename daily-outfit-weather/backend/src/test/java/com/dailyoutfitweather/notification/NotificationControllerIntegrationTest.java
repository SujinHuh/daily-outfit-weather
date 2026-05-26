package com.dailyoutfitweather.notification;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.dailyoutfitweather.support.TestcontainersConfiguration;

@SpringBootTest(properties = "app.notification.generate-due-token=test-token")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class NotificationControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

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
	void rejectGenerateDueLogsWithoutInternalToken() throws Exception {
		mockMvc.perform(post("/api/notifications/generate-due").with(oauth2Login()))
			.andExpect(status().isForbidden());
	}

	@Test
	void generateDueLogsWithInternalTokenReturnsCountOnly() throws Exception {
		insertUserWithProfile("due@example.com", "00:00", "IMPORTANT_ONLY");

		mockMvc.perform(post("/api/notifications/generate-due")
				.with(oauth2Login())
				.header("X-Internal-Job-Token", "test-token"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.generatedCount", is(1)))
			.andExpect(jsonPath("$.body").doesNotExist())
			.andExpect(jsonPath("$.*", not(org.hamcrest.Matchers.hasItem("오늘 추천을 확인할 시간입니다."))));
	}

	private void insertUserWithProfile(String email, String notificationTime, String changeAlertOption) {
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
	}
}
