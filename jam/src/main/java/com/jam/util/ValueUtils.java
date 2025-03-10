package com.jam.util;

public class ValueUtils {
	private ValueUtils() { }  // 생성자 private 처리 (유틸 클래스는 인스턴스화 금지)

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
}
