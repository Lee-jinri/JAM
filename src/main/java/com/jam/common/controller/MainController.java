package com.jam.common.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jam.common.service.MainService;
import com.jam.community.dto.CommunityDto;
import com.jam.fleaMarket.dto.FleaMarketDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final MainService mainService;
	
	@GetMapping("/")
	public String homePage() {
		return "main";
	}
	
	/*************************
	 * 메인 페이지 게시판 글 불러오기
	 * @return 메인 페이지
	 *************************/
	@ResponseBody
	@GetMapping(value = "/boards")
	public ResponseEntity<Map<String, Object>> home() {
		
        //List<RoomRentalVO> roomList = mainService.roomList();
        List<FleaMarketDto> fleaList = mainService.fleaList();
        List<CommunityDto> comList = mainService.comList();

        Map<String, Object> response = new HashMap<>();
        //response.put("roomList", roomList);
        response.put("fleaList", fleaList);
        response.put("comList", comList);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
