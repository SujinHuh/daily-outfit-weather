package com.dailyoutfitweather.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("prod")
public class ProductionConfigurationValidator {

	private final String googleClientId;
	private final String googleClientSecret;
	private final String frontendSuccessUrl;
	private final String notificationToken;
	private final String kmaServiceKey;

	public ProductionConfigurationValidator(
		@Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId,
		@Value("${spring.security.oauth2.client.registration.google.client-secret}") String googleClientSecret,
		@Value("${app.frontend.success-url}") String frontendSuccessUrl,
		@Value("${app.notification.generate-due-token}") String notificationToken,
		@Value("${kma.service-key}") String kmaServiceKey
	) {
		this.googleClientId = googleClientId;
		this.googleClientSecret = googleClientSecret;
		this.frontendSuccessUrl = frontendSuccessUrl;
		this.notificationToken = notificationToken;
		this.kmaServiceKey = kmaServiceKey;
	}

	@PostConstruct
	void validate() {
		requireRealValue("GOOGLE_CLIENT_ID", googleClientId, "dev-google-client-id");
		requireRealValue("GOOGLE_CLIENT_SECRET", googleClientSecret, "dev-google-client-secret");
		requireRealValue("APP_FRONTEND_SUCCESS_URL", frontendSuccessUrl, "http://localhost:5173");
		requireRealValue("APP_NOTIFICATION_GENERATE_DUE_TOKEN", notificationToken, "change-me");
		requireRealValue("KMA_SERVICE_KEY", kmaServiceKey, "");
	}

	private void requireRealValue(String name, String value, String disallowedValue) {
		if (value == null || value.isBlank() || value.equals(disallowedValue) || value.contains("localhost")) {
			throw new IllegalStateException(name + " must be configured for prod profile");
		}
	}
}
