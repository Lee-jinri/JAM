package com.jam.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class ApiUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * SGIS API 인증 토큰 발급
     */
    public static String getAccessToken(String url) {
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response != null && response.has("result")) {
                return response.get("result").get("accessToken").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to fetch access token");
    }

    /**
     * SGIS API를 통해 지역 목록 조회
     */
    public static List<Location> getLocations(String apiUrl, Map<String, String> params) {
        StringBuilder urlWithParams = new StringBuilder(apiUrl + "?");
        params.forEach((k, v) -> urlWithParams.append(k).append("=").append(v).append("&"));

        try {
            JsonNode response = restTemplate.getForObject(urlWithParams.toString(), JsonNode.class);
            if (response != null && response.has("result")) {
                JsonNode result = response.get("result");
                List<Location> locations = new ArrayList<>();

                for (JsonNode node : result) {
                    Location location = new Location();
                    location.setCd(node.get("cd").asText());
                    location.setAddrName(node.get("addr_name").asText());
                    locations.add(location);
                }

                return locations;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to fetch locations");
    }

    /**
     * 내부에서 사용할 Location 클래스 (LocationController에 따로 있어도 무방)
     */
    public static class Location {
        private String cd;
        private String addrName;

        public String getCd() {
            return cd;
        }

        public void setCd(String cd) {
            this.cd = cd;
        }

        public String getAddrName() {
            return addrName;
        }

        public void setAddrName(String addrName) {
            this.addrName = addrName;
        }
    }
}
