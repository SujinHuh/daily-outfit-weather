package com.dailyoutfitweather.global.config;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductionConfigurationValidatorTest {

	@Test
	void allowsPublicIpOriginForCurrentHttpDeployment() {
		ProductionConfigurationValidator validator = new ProductionConfigurationValidator(
			"google-client-id",
			"google-client-secret",
			"http://203.0.113.10",
			"long-random-token",
			"kma-encoding-key"
		);

		assertThatNoException().isThrownBy(validator::validate);
	}

	@Test
	void rejectsPlaceholderValuesForProdProfile() {
		ProductionConfigurationValidator validator = new ProductionConfigurationValidator(
			"change-this-google-client-id",
			"google-client-secret",
			"http://203.0.113.10",
			"long-random-token",
			"kma-encoding-key"
		);

		assertThatThrownBy(validator::validate)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("GOOGLE_CLIENT_ID must be configured for prod profile");
	}

	@Test
	void rejectsExampleDomainForProdProfile() {
		ProductionConfigurationValidator validator = new ProductionConfigurationValidator(
			"google-client-id",
			"google-client-secret",
			"https://daily-outfit-weather.example.com",
			"long-random-token",
			"kma-encoding-key"
		);

		assertThatThrownBy(validator::validate)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("APP_FRONTEND_SUCCESS_URL must be configured for prod profile");
	}
}
