package com.dailyoutfitweather.global.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dailyoutfitweather.user.service.CustomOAuth2UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final String frontendSuccessUrl;
	private final String allowedOrigins;

	public SecurityConfig(
		CustomOAuth2UserService customOAuth2UserService,
		@Value("${app.frontend.success-url}") String frontendSuccessUrl,
		@Value("${app.security.allowed-origins}") String allowedOrigins
	) {
		this.customOAuth2UserService = customOAuth2UserService;
		this.frontendSuccessUrl = frontendSuccessUrl;
		this.allowedOrigins = allowedOrigins;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName(null);

		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(requestHandler)
				.ignoringRequestMatchers(
					"/oauth2/**",
					"/login/**",
					"/api/temp-login",
					"/api/notifications/generate-due"
				)
			)
			.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/health", "/api/auth-options", "/api/temp-login", "/oauth2/**", "/login/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/notifications/generate-due").permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().permitAll()
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				.accessDeniedHandler(accessDeniedHandler())
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

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			System.err.println("Access Denied: " + accessDeniedException.getMessage() + " for " + request.getRequestURI());
			response.sendError(HttpStatus.FORBIDDEN.value(), accessDeniedException.getMessage());
		};
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(parseAllowedOrigins());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "X-Internal-Job-Token"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private List<String> parseAllowedOrigins() {
		List<String> origins = new java.util.ArrayList<>(Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(origin -> !origin.isBlank())
			.toList());
		// Always allow common local development origins to prevent CORS-related 403
		if (!origins.contains("http://localhost:5173")) origins.add("http://localhost:5173");
		if (!origins.contains("http://localhost:8080")) origins.add("http://localhost:8080");
		if (!origins.contains("http://127.0.0.1:5173")) origins.add("http://127.0.0.1:5173");
		if (!origins.contains("http://127.0.0.1:8080")) origins.add("http://127.0.0.1:8080");
		return origins;
	}

	private static class CsrfCookieFilter extends OncePerRequestFilter {

		@Override
		protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
		) throws ServletException, IOException {
			// In Spring Security 6, the CsrfToken is deferred.
			// Accessing it as a request attribute and calling getToken() forces its resolution.
			CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
			if (csrfToken != null) {
				csrfToken.getToken();
			}
			filterChain.doFilter(request, response);
		}
	}
}
