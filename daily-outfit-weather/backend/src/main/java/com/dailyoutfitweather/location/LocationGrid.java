package com.dailyoutfitweather.location;

public record LocationGrid(
	String sido,
	String sigungu,
	String dong,
	Integer nx,
	Integer ny
) {

	boolean matches(String keyword) {
		String normalizedKeyword = normalize(keyword);
		return normalize(sido).contains(normalizedKeyword)
			|| normalize(sigungu).contains(normalizedKeyword)
			|| normalize(dong).contains(normalizedKeyword)
			|| normalize(sido + sigungu + dong).contains(normalizedKeyword)
			|| searchableDongAlias().contains(normalizedKeyword);
	}

	static String key(String sido, String sigungu, String dong) {
		return normalize(sido) + "|" + normalize(sigungu) + "|" + normalize(dong);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
	}

	private String searchableDongAlias() {
		String normalizedDong = normalize(dong);
		return normalizedDong
			.replaceAll("본동$", "동")
			.replaceAll("\\d+가\\d+동$", "동")
			.replaceAll("\\d+동$", "동");
	}
}
