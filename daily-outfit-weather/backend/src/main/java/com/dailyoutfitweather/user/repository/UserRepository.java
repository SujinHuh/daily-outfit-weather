package com.dailyoutfitweather.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dailyoutfitweather.user.domain.User;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByProviderAndProviderId(com.dailyoutfitweather.user.domain.AuthProvider provider, String providerId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.email = :email")
	Optional<User> findByEmailForUpdate(@Param("email") String email);
}
