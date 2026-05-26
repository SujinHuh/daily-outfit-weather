package com.dailyoutfitweather.location;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationSearchController {

	private final LocationGridCatalog locationGridCatalog;

	public LocationSearchController(LocationGridCatalog locationGridCatalog) {
		this.locationGridCatalog = locationGridCatalog;
	}

	@GetMapping("/search")
	List<LocationGrid> search(@RequestParam String keyword) {
		return locationGridCatalog.search(keyword);
	}
}
