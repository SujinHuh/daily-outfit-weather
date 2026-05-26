package com.dailyoutfitweather.user.domain;

import java.time.Instant;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(nullable = false)
	private int coldSensitivity;

	@Column(nullable = false)
	private int heatSensitivity;

	@Column(nullable = false)
	private LocalTime commuteTime;

	@Column(nullable = false)
	private LocalTime leaveWorkTime;

	@Column(nullable = false)
	private LocalTime notificationTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TransportType transportType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private MessageTone messageTone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ChangeAlertOption changeAlertOption;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected UserProfile() {
	}

	public UserProfile(User user) {
		this.user = user;
	}

	public void update(
		int coldSensitivity,
		int heatSensitivity,
		LocalTime commuteTime,
		LocalTime leaveWorkTime,
		LocalTime notificationTime,
		TransportType transportType,
		MessageTone messageTone,
		ChangeAlertOption changeAlertOption
	) {
		this.coldSensitivity = coldSensitivity;
		this.heatSensitivity = heatSensitivity;
		this.commuteTime = commuteTime;
		this.leaveWorkTime = leaveWorkTime;
		this.notificationTime = notificationTime;
		this.transportType = transportType;
		this.messageTone = messageTone;
		this.changeAlertOption = changeAlertOption;
	}

	public User getUser() {
		return user;
	}

	public int getColdSensitivity() {
		return coldSensitivity;
	}

	public int getHeatSensitivity() {
		return heatSensitivity;
	}

	public LocalTime getCommuteTime() {
		return commuteTime;
	}

	public LocalTime getLeaveWorkTime() {
		return leaveWorkTime;
	}

	public LocalTime getNotificationTime() {
		return notificationTime;
	}

	public TransportType getTransportType() {
		return transportType;
	}

	public MessageTone getMessageTone() {
		return messageTone;
	}

	public ChangeAlertOption getChangeAlertOption() {
		return changeAlertOption;
	}
}
