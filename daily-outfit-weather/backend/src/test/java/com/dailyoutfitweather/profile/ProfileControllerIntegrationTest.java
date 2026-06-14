package com.dailyoutfitweather.profile;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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
class ProfileControllerIntegrationTest {

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
	void saveOnboardingAndReadProfile() throws Exception {
			mockMvc.perform(post("/api/profile/onboarding").with(loginUser()).with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(profileRequest("수진", "역삼1동", "판교동")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickname", is("수진")))
				.andExpect(jsonPath("$.coldSensitivity", is(4)))
				.andExpect(jsonPath("$.homeLocation.dong", is("역삼1동")))
				.andExpect(jsonPath("$.homeLocation.nx", is(61)))
				.andExpect(jsonPath("$.homeLocation.ny", is(125)))
				.andExpect(jsonPath("$.workLocation.dong", is("판교동")));

		mockMvc.perform(get("/api/profile").with(loginUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email", is(USER_EMAIL)))
			.andExpect(jsonPath("$.transportType", is("PUBLIC_TRANSPORT")))
			.andExpect(jsonPath("$.homeLocation.sido", is("서울특별시")));
	}

	@Test
	void updateProfileReplacesLocationsPerType() throws Exception {
		mockMvc.perform(post("/api/profile/onboarding").with(loginUser()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileRequest("수진", "역삼동", "성수동")))
			.andExpect(status().isOk());

		mockMvc.perform(put("/api/profile").with(loginUser()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileRequest("변경", "서초동", "판교동")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nickname", is("변경")))
			.andExpect(jsonPath("$.homeLocation.dong", is("서초동")))
			.andExpect(jsonPath("$.workLocation.dong", is("판교동")));

		mockMvc.perform(get("/api/profile").with(loginUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.homeLocation.dong", is("서초동")))
			.andExpect(jsonPath("$.workLocation.dong", is("판교동")));

		Long profileCount = jdbcTemplate.queryForObject("select count(*) from user_profiles", Long.class);
		Long locationCount = jdbcTemplate.queryForObject("select count(*) from locations", Long.class);
		org.assertj.core.api.Assertions.assertThat(profileCount).isEqualTo(1);
		org.assertj.core.api.Assertions.assertThat(locationCount).isEqualTo(2);
	}

	@Test
	void searchLocationsByKeyword() throws Exception {
			mockMvc.perform(get("/api/locations/search").with(loginUser())
					.param("keyword", "역삼"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].sido", hasItem("서울특별시")))
				.andExpect(jsonPath("$[*].sigungu", hasItem("강남구")))
				.andExpect(jsonPath("$[*].dong", hasItem("역삼1동")))
				.andExpect(jsonPath("$[*].nx", hasItem(61)))
				.andExpect(jsonPath("$[*].ny", hasItem(125)));
	}

	@Test
	void rejectInvalidSensitivity() throws Exception {
		String request = profileRequest("수진", "역삼동", "성수동")
			.replace("\"coldSensitivity\": 4", "\"coldSensitivity\": 6");

		mockMvc.perform(post("/api/profile/onboarding").with(loginUser()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
	}

	@Test
	void rejectOverlongNicknameBeforePersistence() throws Exception {
		String request = profileRequest("가".repeat(51), "역삼동", "성수동");

		mockMvc.perform(post("/api/profile/onboarding").with(loginUser()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
	}

	@Test
	void getProfileBeforeOnboardingReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/profile").with(loginUser()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code", is("PROFILE_NOT_FOUND")))
			.andExpect(jsonPath("$.message", notNullValue()));
	}

	@Test
	void updateProfileBeforeOnboardingReturnsNotFound() throws Exception {
		mockMvc.perform(put("/api/profile").with(loginUser()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileRequest("수진", "역삼동", "성수동")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code", is("PROFILE_NOT_FOUND")));
	}

	private RequestPostProcessor loginUser() {
		return oauth2Login().attributes(attributes -> attributes.put("email", USER_EMAIL));
	}

	private String profileRequest(String nickname, String homeDong, String workDong) {
		return """
			{
			  "nickname": "%s",
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
			    "dong": "%s"
			  },
			  "workLocation": {
			    "sido": "경기도",
			    "sigungu": "성남시 분당구",
			    "dong": "%s"
			  }
			}
			""".formatted(nickname, homeDong, workDong);
	}
}
