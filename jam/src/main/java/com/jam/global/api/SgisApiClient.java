package com.jam.global.api;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class SgisApiClient {

	private final RestTemplate restTemplate;

    public SgisApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * SGIS API 인증 토큰 발급
     */
    public String getAccessToken(String url) {
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
    public List<Location> getLocations(String apiUrl, Map<String, String> params) {
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
}
