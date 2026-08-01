package com.dailyoutfitweather.user;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.dailyoutfitweather.support.TestcontainersConfiguration;

@SpringBootTest(properties = {
	"app.temp-login.enabled=true",
	"app.temp-login.password=test-temp-password",
	"app.temp-login.email=temp-user@daily-outfit-weather.local",
	"app.temp-login.nickname=임시 사용자"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerIntegrationTest {

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
	void tempLoginCreatesSessionForConfiguredUser() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/api/temp-login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"password\":\"test-temp-password\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email", is("temp-user@daily-outfit-weather.local")))
			.andReturn();

		MockHttpSession session = (MockHttpSession)loginResult.getRequest().getSession(false);

		mockMvc.perform(get("/api/me").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email", is("temp-user@daily-outfit-weather.local")))
			.andExpect(jsonPath("$.nickname", is("임시 사용자")));
	}

	@Test
	void tempLoginRejectsWrongToken() throws Exception {
		mockMvc.perform(post("/api/temp-login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"password\":\"wrong\"}"))
			.andExpect(status().isUnauthorized());
	}
}
