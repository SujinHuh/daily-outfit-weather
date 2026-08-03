package com.dailyoutfitweather.weather.client;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.dailyoutfitweather.recommendation.dto.WeatherSnapshot;
import com.dailyoutfitweather.weather.dto.KmaForecastItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class KmaWeatherApiClient implements WeatherApiClient {

	private static final Logger log = LoggerFactory.getLogger(KmaWeatherApiClient.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	private final RestClient restClient;
	private final KmaForecastBaseTimeCalculator baseTimeCalculator;
	private final KmaForecastParser forecastParser;
	private final Clock clock;
	private final String serviceKey;
	private final String baseUrl;

	public KmaWeatherApiClient(
		RestClient.Builder restClientBuilder,
		KmaForecastBaseTimeCalculator baseTimeCalculator,
		KmaForecastParser forecastParser,
		Clock clock,
		@Value("${kma.service-key:}") String serviceKey,
		@Value("${kma.base-url:https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0}") String baseUrl,
		@Value("${kma.connect-timeout:2s}") Duration connectTimeout,
		@Value("${kma.read-timeout:3s}") Duration readTimeout
	) {
		if (connectTimeout != null || readTimeout != null) {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			if (connectTimeout != null) {
				requestFactory.setConnectTimeout(connectTimeout);
			}
			if (readTimeout != null) {
				requestFactory.setReadTimeout(readTimeout);
			}
			restClientBuilder.requestFactory(requestFactory);
		}
		this.restClient = restClientBuilder.build();
		this.baseTimeCalculator = baseTimeCalculator;
		this.forecastParser = forecastParser;
		this.clock = clock;
		this.serviceKey = serviceKey;
		this.baseUrl = baseUrl;
	}

	@Override
	public WeatherSnapshot getForecast(int nx, int ny, LocalDate targetDate, LocalTime targetTime) {
		if (!StringUtils.hasText(serviceKey)) {
			throw new WeatherApiException("KMA service key is not configured");
		}
		ForecastBaseTime baseTime = baseTimeCalculator.baseTimeForTarget(clock, targetDate, targetTime);
		String query = UriComponentsBuilder.newInstance()
			.queryParam("pageNo", 1)
			.queryParam("numOfRows", 1000)
			.queryParam("dataType", "JSON")
			.queryParam("base_date", baseTime.date().format(DATE_FORMATTER))
			.queryParam("base_time", baseTime.time())
			.queryParam("nx", nx)
			.queryParam("ny", ny)
			.build()
			.toUriString();
		String url = forecastEndpoint() + "?serviceKey=" + serviceKey + "&" + query.substring(1);

		try {
			KmaForecastResponse response = restClient.get()
				.uri(URI.create(url))
				.retrieve()
				.body(KmaForecastResponse.class);
			List<KmaForecastItem> items = extractItems(response);
			WeatherSnapshot snapshot = forecastParser.parse(items, targetDate, targetTime);
			log.info("Successfully fetched KMA weather forecast for grid (nx={}, ny={}), targetDate={}, targetTime={}: temperature={}, feelsLike={}",
				nx, ny, targetDate, targetTime, snapshot.temperature(), snapshot.feelsLikeTemperature());
			return snapshot;
		}
		catch (WeatherApiException exception) {
			throw exception;
		}
		catch (RuntimeException exception) {
			throw new WeatherApiException("Failed to call KMA forecast API", exception);
		}
	}

	private String forecastEndpoint() {
		String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		return normalizedBaseUrl + "/getVilageFcst";
	}

	private List<KmaForecastItem> extractItems(KmaForecastResponse response) {
		if (response == null || response.response() == null) {
			throw new WeatherApiException("KMA forecast response is empty");
		}
		Header header = response.response().header();
		if (header != null && header.resultCode() != null && !"00".equals(header.resultCode())) {
			throw new WeatherApiException("KMA forecast API failed: " + header.resultCode() + " " + header.resultMsg());
		}
		if (response.response().body() == null || response.response().body().items() == null
			|| response.response().body().items().item() == null) {
			throw new WeatherApiException("KMA forecast response is empty");
		}
		return response.response().body().items().item();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record KmaForecastResponse(Response response) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Response(Header header, Body body) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Header(String resultCode, String resultMsg) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Body(Items items) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Items(List<KmaForecastItem> item) {
	}
}
