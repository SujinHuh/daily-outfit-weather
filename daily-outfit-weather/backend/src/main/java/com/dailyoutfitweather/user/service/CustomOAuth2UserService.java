package com.dailyoutfitweather.user.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailyoutfitweather.user.domain.AuthProvider;
import com.dailyoutfitweather.user.domain.User;
import com.dailyoutfitweather.user.repository.UserRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;

	public CustomOAuth2UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String providerId = String.valueOf(attributes.get(userNameAttributeName));
		String email = (String)attributes.get("email");
		String nickname = (String)attributes.get("name");

		User user = userRepository.findByProviderAndProviderId(provider, providerId)
			.orElseGet(() -> userRepository.findByEmail(email)
				.map(existingUser -> {
					existingUser.connectOAuth(provider, providerId, nickname);
					return existingUser;
				})
				.orElseGet(() -> userRepository.save(new User(email, nickname, provider, providerId))));

		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
			attributes,
			userNameAttributeName
		);
	}
}
