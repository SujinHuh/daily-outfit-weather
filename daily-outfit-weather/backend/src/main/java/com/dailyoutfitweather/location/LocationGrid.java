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
			|| normalize(sido + sigungu + dong).contains(normalizedKeyword);
	}

	static String key(String sido, String sigungu, String dong) {
		return normalize(sido) + "|" + normalize(sigungu) + "|" + normalize(dong);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
	}
}
