package com.dailyoutfitweather.global.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.repository.UserRepository;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	private final UserRepository userRepository;

	public LoginUserArgumentResolver(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUser.class) &&
			parameter.getParameterType().equals(User.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
			throw unauthorized();
		}

		String email = (String)oAuth2User.getAttributes().get("email");
		if (email == null || email.isBlank()) {
			throw unauthorized();
		}

		return userRepository.findByEmail(email).orElseThrow(this::unauthorized);
	}

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
	}
}
