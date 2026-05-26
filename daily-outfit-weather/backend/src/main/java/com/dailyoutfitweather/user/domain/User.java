package com.dailyoutfitweather.user.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = {
	@UniqueConstraint(name = "uk_users_provider_provider_id", columnNames = {"provider", "provider_id"})
})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AuthProvider provider;

	@Column(nullable = false, length = 255)
	private String providerId;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected User() {
	}

	public User(String email, String nickname, AuthProvider provider, String providerId) {
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.providerId = providerId;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void connectOAuth(AuthProvider provider, String providerId, String nickname) {
		this.provider = provider;
		this.providerId = providerId;
		this.nickname = nickname;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getNickname() {
		return nickname;
	}
}
