package com.jam.client.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/community")
@AllArgsConstructor
@Slf4j
public class CommunityController {
	
	/*************************
	 * @return 커뮤니티 글 리스트 페이지
	 */
	@GetMapping("/board")
	public String communityBoards() {
		return "community/board";
	}
	
	/**************************************************
	 * 커뮤니티 글의 상세 페이지를 반환하는 메서드 입니다.
	 * 
	 * @param postId 조회할 커뮤니티의 글 번호
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
	public String writeView(){
		return "community/write";
	}
	
	/********************************
	 *  커뮤니티 글의 수정 페이지를 반환하는 메서드 입니다.
	 *  
	 * @param postId 수정할 커뮤니티의 글 번호
	 * @return 커뮤니티 글 수정 페이지
	 *********************************/
	@GetMapping("/post/edit/{postId}")
	public String updateView(@PathVariable("postId") Long postId, Model model) throws Exception{
		model.addAttribute("postId", postId);
		return "community/update";
	}
	
	/*********************************
	 * @return 작성한 커뮤니티 글 페이지 
	 *********************************/
	@GetMapping(value="/my")
	public String viewPosts(){
		
		return "community/myPosts";
	}
	
	/*********************************
	 * @return 북마크한 커뮤니티 글 페이지 
	 *********************************/
	@GetMapping(value="/my/favorites")
	public String favoritesPosts(){
		
		return "community/favorite";
	}
}