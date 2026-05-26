package com.dailyoutfitweather.notification.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import com.dailyoutfitweather.global.security.LoginUser;
import com.dailyoutfitweather.notification.dto.GenerateDueNotificationLogsResponse;
import com.dailyoutfitweather.notification.dto.NotificationLogResponse;
import com.dailyoutfitweather.notification.service.NotificationLogService;
import com.dailyoutfitweather.user.domain.User;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationLogService notificationLogService;
	private final String generateDueToken;

	public NotificationController(
		NotificationLogService notificationLogService,
		@Value("${app.notification.generate-due-token}") String generateDueToken
	) {
		this.notificationLogService = notificationLogService;
		this.generateDueToken = generateDueToken;
	}

	@GetMapping
	List<NotificationLogResponse> getLogs(@LoginUser User user) {
		return notificationLogService.getLogs(user);
	}

	@GetMapping("/today")
	List<NotificationLogResponse> getTodayLogs(@LoginUser User user) {
		return notificationLogService.getTodayLogs(user);
	}

	@PostMapping("/generate-due")
	GenerateDueNotificationLogsResponse generateDueLogs(
		@RequestHeader(value = "X-Internal-Job-Token", required = false) String internalJobToken
	) {
		if (generateDueToken.isBlank() || !generateDueToken.equals(internalJobToken)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "알림 로그 생성 권한이 없습니다.");
		}
		return new GenerateDueNotificationLogsResponse(notificationLogService.generateDueLogCount());
	}
}
