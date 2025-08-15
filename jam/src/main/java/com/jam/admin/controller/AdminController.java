package com.jam.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.global.jwt.JwtService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@RequestMapping("/admin")
@RequiredArgsConstructor
@Controller
public class AdminController {
	private final JwtService jwtService;

	@GetMapping("/admin")
	public String doAdmin(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		List<String> roles = jwtService.extractUserRole(request, cookies);
		
		if (!roles.contains("ROLE_ADMIN")) {
		    return "redirect:/";
		}
		
		return "/admin/admin";
	}
	
	@Value("${location.save.path}")
    private String savePath;
	
	@Value("${location.consumer_key}")
	private String consumer_key;
	
	@Value("${location.consumer_secret}")
	private String consumer_secret;
	

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken = null;

    @PostMapping("/updateAreas")
    @ResponseBody
    public Map<String, Object> updateAllAreas() {

        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Access Token 발급
            getAccessToken();

            // 2. 전국 시, 구, 동 데이터 수집
            Map<String, Map<String, List<String>>> areaMap = new LinkedHashMap<>();

            // 시/도 하드코딩 리스트 (data-index 값 기준)
            Map<String, String> cities = new LinkedHashMap<>();
            cities.put("서울", "11");
            cities.put("부산", "21");
            cities.put("대구", "22");
            cities.put("인천", "23");
            cities.put("광주", "24");
            cities.put("대전", "25");
            cities.put("울산", "26");
            cities.put("세종", "29");
            cities.put("경기", "31");
            cities.put("강원", "32");
            cities.put("충북", "33");
            cities.put("충남", "34");
            cities.put("전북", "35");
            cities.put("전남", "36");
            cities.put("경북", "37");
            cities.put("경남", "38");
            cities.put("제주", "39");

            // 시/도별 구/동 데이터 수집
            for (Map.Entry<String, String> entry : cities.entrySet()) {
                String cityName = entry.getKey();
                String cityCd = entry.getValue();
                
                Map<String, List<String>> guMap = new LinkedHashMap<>();

                // 구 조회
                List<Map<String, String>> gus = fetchLocations(cityCd);

                for (Map<String, String> gu : gus) {
                    String guName = gu.get("addr_name");
                    String guCd = gu.get("cd");

                    // 동 조회
                    List<Map<String, String>> dongs = fetchLocations(guCd);
                    List<String> dongNames = new ArrayList<>();

                    for (Map<String, String> dong : dongs) {
                        dongNames.add(dong.get("addr_name"));
                    }

                    guMap.put(guName, dongNames);
                }

                areaMap.put(cityName, guMap);
            }

            // 3. JSON 저장
            saveAsJson(areaMap);

            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }


    private void getAccessToken() {
        String url = "https://sgisapi.kostat.go.kr/OpenAPI3/auth/authentication.json"
                   + "?consumer_key=" + consumer_key 
                   + "&consumer_secret=" + consumer_secret;

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        if (response != null && response.has("result")) {
            accessToken = response.get("result").get("accessToken").asText();
        } else {
            throw new RuntimeException("Failed to fetch access token");
        }
        
    }

    private List<Map<String, String>> fetchLocations(String cd) {
        String url = "https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json"
                   + "?accessToken=" + accessToken
                   + "&cd=" + cd;

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        List<Map<String, String>> locations = new ArrayList<>();
        
        if (response != null && response.has("result")) {
            for (JsonNode node : response.get("result")) {
                Map<String, String> location = new HashMap<>();
                location.put("cd", node.get("cd").asText());
                location.put("addr_name", node.get("addr_name").asText());
                locations.add(location);
            }
        }
        return locations;
    }

    private void saveAsJson(Map<String, Map<String, List<String>>> data) throws IOException {
        File file = new File(savePath + "/locationData.json");
        file.getParentFile().mkdirs();
        objectMapper.writeValue(file, data);
    }

}
