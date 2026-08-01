package com.dailyoutfitweather.user.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dailyoutfitweather.global.security.LoginUser;
import com.dailyoutfitweather.user.domain.AuthProvider;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.dto.UserResponse;
import com.dailyoutfitweather.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class AuthController {

	private final UserRepository userRepository;
	private final boolean tempLoginEnabled;
	private final String tempLoginPassword;
	private final String tempLoginEmail;
	private final String tempLoginNickname;

	public AuthController(
		UserRepository userRepository,
		@Value("${app.temp-login.enabled:false}") boolean tempLoginEnabled,
		@Value("${app.temp-login.password:}") String tempLoginPassword,
		@Value("${app.temp-login.email:temp-user@daily-outfit-weather.local}") String tempLoginEmail,
		@Value("${app.temp-login.nickname:임시 사용자}") String tempLoginNickname
	) {
		this.userRepository = userRepository;
		this.tempLoginEnabled = tempLoginEnabled;
		this.tempLoginPassword = tempLoginPassword;
		this.tempLoginEmail = tempLoginEmail;
		this.tempLoginNickname = tempLoginNickname;
	}

	@GetMapping("/me")
	public UserResponse me(@LoginUser User user) {
		return UserResponse.from(user);
	}

	@GetMapping("/auth-options")
	public AuthOptionsResponse authOptions() {
		return new AuthOptionsResponse(isTempLoginAvailable());
	}

	@PostMapping("/temp-login")
	public UserResponse tempLogin(@RequestBody TempLoginRequest request, HttpServletRequest servletRequest) {
		if (!isTempLoginAvailable()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "임시 로그인이 비활성화되어 있습니다.");
		}
		if (request == null || !tempLoginPassword.equals(request.passwordOrToken())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "임시 로그인 비밀번호가 올바르지 않습니다.");
		}

		User user = userRepository.findByEmail(tempLoginEmail)
			.orElseGet(() -> userRepository.save(new User(
				tempLoginEmail,
				tempLoginNickname,
				AuthProvider.DEV,
				tempLoginEmail
			)));

		OAuth2User principal = new DefaultOAuth2User(
			java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")),
			Map.of(
				"sub", "temp-login",
				"email", user.getEmail(),
				"name", user.getNickname()
			),
			"sub"
		);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			principal,
			null,
			principal.getAuthorities()
		);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		servletRequest.getSession(true).setAttribute(
			HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
			context
		);

		return UserResponse.from(user);
	}

	private boolean isTempLoginAvailable() {
		return tempLoginEnabled && tempLoginPassword != null && !tempLoginPassword.isBlank();
	}

	public record AuthOptionsResponse(boolean tempLoginEnabled) {
	}

	public record TempLoginRequest(String password, String token) {

		String passwordOrToken() {
			if (password != null) {
				return password;
			}
			return token;
		}
	}
}
