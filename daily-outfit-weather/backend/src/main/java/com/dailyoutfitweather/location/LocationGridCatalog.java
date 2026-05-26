package com.dailyoutfitweather.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

@Component
public class LocationGridCatalog {

	private static final String RESOURCE_PATH = "location/kma_location_grids.csv";

	private List<LocationGrid> grids = List.of();
	private Map<String, LocationGrid> gridByKey = Map.of();

	@PostConstruct
	void load() throws IOException {
		List<LocationGrid> loaded = new ArrayList<>();
		ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] columns = line.split(",", -1);
				if (columns.length != 5) {
					continue;
				}
				loaded.add(new LocationGrid(
					columns[0].trim(),
					columns[1].trim(),
					columns[2].trim(),
					Integer.valueOf(columns[3].trim()),
					Integer.valueOf(columns[4].trim())
				));
			}
		}
		this.grids = List.copyOf(loaded);
		this.gridByKey = loaded.stream()
			.collect(Collectors.toUnmodifiableMap(
				grid -> LocationGrid.key(grid.sido(), grid.sigungu(), grid.dong()),
				Function.identity(),
				(existing, replacement) -> existing
			));
	}

	public Optional<LocationGrid> find(String sido, String sigungu, String dong) {
		return Optional.ofNullable(gridByKey.get(LocationGrid.key(sido, sigungu, dong)));
	}

	public List<LocationGrid> search(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return List.of();
		}
		return grids.stream()
			.filter(grid -> grid.matches(keyword))
			.limit(20)
			.toList();
	}
}
