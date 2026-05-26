package com.dailyoutfitweather.user.domain;

import java.time.Instant;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "locations", uniqueConstraints = {
	@UniqueConstraint(name = "uk_locations_user_type", columnNames = {"user_id", "type"})
})
public class Location {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LocationType type;

	@Column(nullable = false, length = 50)
	private String sido;

	@Column(nullable = false, length = 50)
	private String sigungu;

	@Column(nullable = false, length = 50)
	private String dong;

	private Integer nx;

	private Integer ny;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected Location() {
	}

	public Location(User user, LocationType type, String sido, String sigungu, String dong, Integer nx, Integer ny) {
		this.user = user;
		this.type = type;
		update(sido, sigungu, dong, nx, ny);
	}

	public void update(String sido, String sigungu, String dong, Integer nx, Integer ny) {
		this.sido = sido;
		this.sigungu = sigungu;
		this.dong = dong;
		this.nx = nx;
		this.ny = ny;
	}

	public Long getId() {
		return id;
	}

	public LocationType getType() {
		return type;
	}

	public String getSido() {
		return sido;
	}

	public String getSigungu() {
		return sigungu;
	}

	public String getDong() {
		return dong;
	}

	public Integer getNx() {
		return nx;
	}

	public Integer getNy() {
		return ny;
	}
}
