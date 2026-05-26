package com.dailyoutfitweather.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.dailyoutfitweather.support.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RecommendationControllerIntegrationTest {

	private static final String USER_EMAIL = "sujin@example.com";

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
		jdbcTemplate.update(
			"insert into users (email, nickname, provider, provider_id, created_at, updated_at) values (?, ?, ?, ?, now(), now())",
			USER_EMAIL, "수진", "GOOGLE", "google-user"
		);
	}

	@Test
	void getTodayRecommendationCreatesAndStoresRecommendation() throws Exception {
		saveOnboarding();

		mockMvc.perform(get("/api/recommendations/today").with(loginUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", notNullValue()))
			.andExpect(jsonPath("$.targetDate", is(LocalDate.now(java.time.Clock.system(java.time.ZoneId.of("Asia/Seoul"))).toString())))
			.andExpect(jsonPath("$.summaryMessage", notNullValue()))
			.andExpect(jsonPath("$.characterImageType", notNullValue()))
			.andExpect(jsonPath("$.topRecommendation", notNullValue()))
			.andExpect(jsonPath("$.reason", notNullValue()))
			.andExpect(jsonPath("$.weatherSummary.commuteFeelsLike", is(16)))
			.andExpect(jsonPath("$.weatherSummary.leaveWorkFeelsLike", is(13)))
			.andExpect(jsonPath("$.weatherSummary.rainProbability", is(30)))
			.andExpect(jsonPath("$.weatherSummary.windSpeed", is(2.8)));

		Long count = jdbcTemplate.queryForObject("select count(*) from outfit_recommendations", Long.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void postTodayRecommendationReturnsExistingRecommendationWhenAlreadyCreated() throws Exception {
		saveOnboarding();

		MvcResult first = mockMvc.perform(post("/api/recommendations/today").with(loginUser()))
			.andExpect(status().isOk())
			.andReturn();
		MvcResult second = mockMvc.perform(post("/api/recommendations/today").with(loginUser()))
			.andExpect(status().isOk())
			.andReturn();

		String firstBody = first.getResponse().getContentAsString();
		String secondBody = second.getResponse().getContentAsString();
		assertThat(secondBody).isEqualTo(firstBody);

		Long count = jdbcTemplate.queryForObject("select count(*) from outfit_recommendations", Long.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void getTodayRecommendationBeforeOnboardingReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/recommendations/today").with(loginUser()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code", is("PROFILE_NOT_FOUND")));
	}

	private RequestPostProcessor loginUser() {
		return oauth2Login().attributes(attributes -> attributes.put("email", USER_EMAIL));
	}

	private void saveOnboarding() throws Exception {
		mockMvc.perform(post("/api/profile/onboarding").with(loginUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "nickname": "수진",
					  "coldSensitivity": 4,
					  "heatSensitivity": 2,
					  "commuteTime": "08:30",
					  "leaveWorkTime": "18:30",
					  "notificationTime": "07:30",
					  "transportType": "PUBLIC_TRANSPORT",
					  "messageTone": "CHARACTER",
					  "changeAlertOption": "IMPORTANT_ONLY",
					  "homeLocation": {
					    "sido": "서울특별시",
					    "sigungu": "강남구",
					    "dong": "역삼동"
					  },
					  "workLocation": {
					    "sido": "경기도",
					    "sigungu": "성남시 분당구",
					    "dong": "판교동"
					  }
					}
					"""))
			.andExpect(status().isOk());
	}
}
