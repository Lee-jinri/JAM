package com.jam.client.community.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.jam.client.community.service.CommunityService;
import com.jam.client.community.vo.CommunityVO;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/community")
@AllArgsConstructor
@Slf4j
public class CommunityController {
	
	private final CommunityService comService;
	
	/*************************
	 * @return 커뮤니티 글 리스트 페이지
	 */
	@GetMapping("/board")
	public String communityBoards() {
		return "community/board";
	}
	
	/**************************************************
	 * 커뮤니티 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param com_no 조회할 커뮤니티의 글 번호
	 * @return 커뮤니티 상세 페이지
	 **************************************************/
	@GetMapping("/post/{postId}")
	public String communityDetail(@PathVariable("postId") Long postId, Model model) {
		model.addAttribute("postId", postId);
		
	    return "community/post";
	}
	
	
	/***************************************
	 * @return 커뮤니티 글 작성 페이지
	 ***************************************/
	@GetMapping("/post/write")
	public String writeView() throws Exception{
				
		return "community/write";
	}
	
	/********************************
	 *  커뮤니티 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param com_no 수정할 커뮤니티의 글 번호
	 * @return 커뮤니티 글 수정 페이지
	 *********************************/
	@GetMapping("/post/edit/{postId}")
	public String updateView(@PathVariable("postId") Long postId, Model model) throws Exception{
		model.addAttribute("postId", postId);
		return "community/update";
	}
	
	/*********************************
	 * @return 작성한 커뮤니티 글 페이지 
	 * @throws Exception 
	 *********************************/
	@GetMapping(value="/my")
	public String viewPosts() throws Exception {
		
		return "community/myPosts";
	}
}