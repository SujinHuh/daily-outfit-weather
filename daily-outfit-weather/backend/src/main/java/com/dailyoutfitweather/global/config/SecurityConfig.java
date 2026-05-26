package com.dailyoutfitweather.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import com.dailyoutfitweather.user.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final String frontendSuccessUrl;

	public SecurityConfig(
		CustomOAuth2UserService customOAuth2UserService,
		@Value("${app.frontend.success-url}") String frontendSuccessUrl
	) {
		this.customOAuth2UserService = customOAuth2UserService;
		this.frontendSuccessUrl = frontendSuccessUrl;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/health", "/oauth2/**", "/login/**").permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().permitAll()
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
			)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.defaultSuccessUrl(frontendSuccessUrl, true)
			)
			.logout(logout -> logout
				.logoutUrl("/api/logout")
				.logoutSuccessHandler((request, response, authentication) -> {
					response.setStatus(HttpStatus.OK.value());
				})
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
			);

		return http.build();
	}
}
