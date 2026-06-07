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

	@Test
	void searchesSoutheastSeoulNeighborhoodsByDongKeyword() {
		assertThat(catalog.search("성내"))
			.extracting(LocationGrid::dong)
			.containsExactly("성내1동", "성내2동", "성내3동");
		assertThat(catalog.search("둔촌"))
			.extracting(LocationGrid::dong)
			.containsExactly("둔촌1동", "둔촌2동");
		assertThat(catalog.search("잠실"))
			.extracting(LocationGrid::dong)
			.containsExactly("잠실본동", "잠실2동", "잠실3동", "잠실4동", "잠실6동", "잠실7동");
	}
}
