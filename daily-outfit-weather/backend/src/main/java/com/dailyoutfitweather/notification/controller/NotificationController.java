package com.dailyoutfitweather.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dailyoutfitweather.global.security.LoginUser;
import com.dailyoutfitweather.notification.dto.NotificationLogResponse;
import com.dailyoutfitweather.notification.service.NotificationLogService;
import com.dailyoutfitweather.user.domain.User;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationLogService notificationLogService;

	public NotificationController(NotificationLogService notificationLogService) {
		this.notificationLogService = notificationLogService;
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
	List<NotificationLogResponse> generateDueLogs() {
		return notificationLogService.generateDueLogs();
	}
}
