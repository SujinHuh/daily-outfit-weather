package com.dailyoutfitweather.recommendation;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.dailyoutfitweather.support.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FeedbackControllerIntegrationTest {

	private static final String USER_EMAIL = "sujin@example.com";
	private static final String OTHER_EMAIL = "other@example.com";

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
		insertUser(USER_EMAIL, "google-user");
		insertUser(OTHER_EMAIL, "google-other");
	}

	@Test
	void saveFeedbackForOwnRecommendation() throws Exception {
		Long recommendationId = insertRecommendation(USER_EMAIL);

		mockMvc.perform(post("/api/recommendations/{recommendationId}/feedback", recommendationId).with(loginUser(USER_EMAIL)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "temperatureFeedback": "COLD",
					  "rainFeedback": "NEEDED",
					  "comment": "퇴근길에 추웠어요."
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.recommendationId", is(recommendationId.intValue())))
			.andExpect(jsonPath("$.temperatureFeedback", is("COLD")))
			.andExpect(jsonPath("$.rainFeedback", is("NEEDED")))
			.andExpect(jsonPath("$.comment", is("퇴근길에 추웠어요.")));
	}

	@Test
	void updateExistingFeedbackForSameRecommendation() throws Exception {
		Long recommendationId = insertRecommendation(USER_EMAIL);
		saveFeedback(recommendationId, "COLD");

		mockMvc.perform(post("/api/recommendations/{recommendationId}/feedback", recommendationId).with(loginUser(USER_EMAIL)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "temperatureFeedback": "GOOD",
					  "rainFeedback": "NOT_NEEDED"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.temperatureFeedback", is("GOOD")))
			.andExpect(jsonPath("$.rainFeedback", is("NOT_NEEDED")));

		Long feedbackCount = jdbcTemplate.queryForObject("select count(*) from recommendation_feedbacks", Long.class);
		org.assertj.core.api.Assertions.assertThat(feedbackCount).isEqualTo(1);
	}

	@Test
	void rejectFeedbackForOtherUsersRecommendation() throws Exception {
		Long otherRecommendationId = insertRecommendation(OTHER_EMAIL);

		mockMvc.perform(post("/api/recommendations/{recommendationId}/feedback", otherRecommendationId).with(loginUser(USER_EMAIL)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "temperatureFeedback": "HOT"
					}
					"""))
			.andExpect(status().isNotFound());
	}

	@Test
	void rejectEmptyFeedback() throws Exception {
		Long recommendationId = insertRecommendation(USER_EMAIL);

		mockMvc.perform(post("/api/recommendations/{recommendationId}/feedback", recommendationId).with(loginUser(USER_EMAIL)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());
	}

	private void saveFeedback(Long recommendationId, String temperatureFeedback) throws Exception {
		mockMvc.perform(post("/api/recommendations/{recommendationId}/feedback", recommendationId).with(loginUser(USER_EMAIL)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "temperatureFeedback": "%s"
					}
					""".formatted(temperatureFeedback)))
			.andExpect(status().isOk());
	}

	private RequestPostProcessor loginUser(String email) {
		return oauth2Login().attributes(attributes -> attributes.put("email", email));
	}

	private void insertUser(String email, String providerId) {
		jdbcTemplate.update(
			"insert into users (email, nickname, provider, provider_id, created_at, updated_at) values (?, ?, ?, ?, now(), now())",
			email, "수진", "GOOGLE", providerId
		);
	}

	private Long insertRecommendation(String email) {
		Long userId = jdbcTemplate.queryForObject("select id from users where email = ?", Long.class, email);
		return jdbcTemplate.queryForObject("""
			insert into outfit_recommendations (
			  user_id, target_date, summary_message, top_recommendation, outer_recommendation,
			  item_recommendation, character_image_type, reason, recommendation_type, weather_snapshot, created_at
			) values (?, current_date, '오늘은 가볍게 입어요.', '셔츠', '재킷', '우산', 'LIGHT_OUTER', '테스트', 'DAILY',
			  '{"commuteWeather":{"temperature":17,"feelsLikeTemperature":16,"rainProbability":20,"precipitationType":"NONE","windSpeed":2.4},"leaveWorkWeather":{"temperature":14,"feelsLikeTemperature":13,"rainProbability":30,"precipitationType":"RAIN","windSpeed":2.8}}'::jsonb,
			  now()
			) returning id
			""", Long.class, userId);
	}
}
