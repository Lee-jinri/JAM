package com.jam.global.util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecurityUtil {
	private SecurityUtil() {}

    public static String hashToken(String token) {
    	if (token == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("토큰 해싱 중 오류 발생", e);
        }
    }
    public static boolean matches(String rawToken, String hashedToken) {
        // 원본을 다시 해싱해서 같은지 비교
        String newHash = hashToken(rawToken);
        return newHash.equals(hashedToken);
    }
}
