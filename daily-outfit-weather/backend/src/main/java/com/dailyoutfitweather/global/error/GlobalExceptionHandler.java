package com.dailyoutfitweather.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dailyoutfitweather.profile.service.ProfileNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProfileNotFoundException.class)
	ResponseEntity<ErrorResponse> handleProfileNotFound(ProfileNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ErrorResponse("PROFILE_NOT_FOUND", exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		return ResponseEntity.badRequest()
			.body(new ErrorResponse("VALIDATION_FAILED", "요청 값이 올바르지 않습니다."));
	}
}
