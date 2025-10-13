package com.jam.global.util;

public class ValueUtils {
	private ValueUtils() { }  

    public static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }

    public static String guNullToAll(String value) {
        if (value == null) {
            return "전체";
        }
        return value;
    }
    
    public static String sanitizeForLike(String keyword) {
		if (keyword == null) return null;

		// 1 트림
		String k = keyword.trim();
		if (k.isEmpty()) return null;

		// 2 길이 제한 50자
		if (k.length() > 50) {
			k = k.substring(0, 50);
		}

		// 3 유니코드 정규화
		k = java.text.Normalizer.normalize(k, java.text.Normalizer.Form.NFKC);

		// 4 제어문자, 공백 제거
		k = k.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s{2,}", " ").trim();

		// 5 LIKE 와일드카드, ESCAPE 문자 이스케이프 (%, _, \)
		k = k.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");

		// 6 양쪽 % 
		return "%" + k + "%";
	}
    
}
