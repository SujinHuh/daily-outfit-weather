package com.dailyoutfitweather.location;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocationGridCatalogTest {

	private LocationGridCatalog catalog;

	@BeforeEach
	void setUp() throws Exception {
		catalog = new LocationGridCatalog();
		catalog.load();
	}

	@Test
	void findsGridByExactAdministrativeNames() {
		LocationGrid grid = catalog.find("서울특별시", "강남구", "역삼동").orElseThrow();

		assertThat(grid.nx()).isEqualTo(61);
		assertThat(grid.ny()).isEqualTo(125);
	}

	@Test
	void searchesByDongKeyword() {
		assertThat(catalog.search("판교"))
			.extracting(LocationGrid::dong)
			.contains("판교동");
	}
}
