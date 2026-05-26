package com.dailyoutfitweather.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

import com.dailyoutfitweather.recommendation.dto.PrecipitationType;
import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.weather.client.FeelsLikeTemperatureCalculator;
import com.dailyoutfitweather.weather.client.KmaForecastBaseTimeCalculator;
import com.dailyoutfitweather.weather.client.KmaForecastParser;
import com.dailyoutfitweather.weather.client.KmaWeatherApiClient;
import com.dailyoutfitweather.weather.client.WeatherApiException;

class KmaWeatherApiClientTest {

	@Test
	void callsKmaVilageForecastWithEncodedServiceKeyAndParsesResponse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KmaWeatherApiClient client = client(builder, "abc%2B%2F%3D%3D");
		server.expect(requestTo("http://kma.test/getVilageFcst?serviceKey=abc%2B%2F%3D%3D&pageNo=1&numOfRows=1000&dataType=JSON&base_date=20260525&base_time=1400&nx=61&ny=125"))
			.andRespond(withSuccess("""
				{
				  "response": {
				    "header": {"resultCode": "00", "resultMsg": "NORMAL_SERVICE"},
				    "body": {"items": {"item": [
				      {"category": "TMP", "fcstDate": "20260525", "fcstTime": "1900", "fcstValue": "14"},
				      {"category": "POP", "fcstDate": "20260525", "fcstTime": "1900", "fcstValue": "80"},
				      {"category": "PTY", "fcstDate": "20260525", "fcstTime": "1900", "fcstValue": "1"},
				      {"category": "WSD", "fcstDate": "20260525", "fcstTime": "1900", "fcstValue": "4.2"}
				    ]}}
				  }
				}
				""", MediaType.APPLICATION_JSON));

		WeatherSnapshot snapshot = client.getForecast(61, 125, LocalDate.of(2026, 5, 25), LocalTime.of(18, 30));

		assertThat(snapshot.temperature()).isEqualTo(14);
		assertThat(snapshot.feelsLikeTemperature()).isEqualTo(12);
		assertThat(snapshot.rainProbability()).isEqualTo(80);
		assertThat(snapshot.precipitationType()).isEqualTo(PrecipitationType.RAIN);
		server.verify();
	}

	@Test
	void failsWhenKmaResultCodeIsNotNormal() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KmaWeatherApiClient client = client(builder, "abc%2B%2F%3D%3D");
		server.expect(requestTo("http://kma.test/getVilageFcst?serviceKey=abc%2B%2F%3D%3D&pageNo=1&numOfRows=1000&dataType=JSON&base_date=20260525&base_time=1400&nx=61&ny=125"))
			.andRespond(withSuccess("""
				{"response":{"header":{"resultCode":"03","resultMsg":"NO_DATA"},"body":{"items":{"item":[]}}}}
				""", MediaType.APPLICATION_JSON));

		assertThatThrownBy(() -> client.getForecast(61, 125, LocalDate.of(2026, 5, 25), LocalTime.of(18, 30)))
			.isInstanceOf(WeatherApiException.class)
			.hasMessageContaining("03");
		server.verify();
	}

	private KmaWeatherApiClient client(RestClient.Builder builder, String serviceKey) {
		Clock clock = Clock.fixed(Instant.from(java.time.OffsetDateTime.parse("2026-05-25T14:10:00+09:00")), ZoneId.of("Asia/Seoul"));
		return new KmaWeatherApiClient(
			builder,
			new KmaForecastBaseTimeCalculator(),
			new KmaForecastParser(new FeelsLikeTemperatureCalculator()),
			clock,
			serviceKey,
			"http://kma.test",
			null,
			null
		);
	}
}
