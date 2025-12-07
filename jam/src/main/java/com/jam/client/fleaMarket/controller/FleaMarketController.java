package com.jam.client.fleaMarket.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jam.client.fleaMarket.service.FleaMarketService;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.common.vo.PageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/fleaMarket")
@RequiredArgsConstructor
@Log4j

public class FleaMarketController {
	
	private final FleaMarketService fleaService;
	
	/**************************************
	 * @param FleaMarketVO flea_vo
	 * @return 중고악기 글 리스트 
	 ***************************************/
	@GetMapping("/board")
	public String fleaMarketList(Model model, @ModelAttribute FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> fleaMarketList = fleaService.getBoard(flea_vo);
		
		model.addAttribute("fleaMarketList",fleaMarketList);
		
		int total = fleaService.listCnt(flea_vo);
		model.addAttribute("pageMaker", new PageDTO(flea_vo, total));
		return "fleaMarket/board";
	}
	
	/**************************************************
	 * 중고악기 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param com_no 조회할 중고악기 글 번호
	 * @return 중고악기 상세 페이지
	 **************************************************/
	@GetMapping("/post/{post_id}")
	public String fleaDetail(@PathVariable("post_id") Long post_id, Model model) {
		model.addAttribute("post_id", post_id);
		
	    return "fleaMarket/post";
	}
	
	/***************************************
	 * @return 중고악기 글 작성 페이지
	 ***************************************/
	@GetMapping("/board/write")
	public String writeView() throws Exception{
				
		return "fleaMarket/write";
	}
	
	/******************************
	 * 중고악기 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param flea_no 수정할 중고악기 글 번호
	 * @return 중고악기 글 수정 페이지
	 ******************************/
	@GetMapping("/post/edit/{postId}")
	public String updateView(@PathVariable("postId") Long postId, Model model) throws Exception{
	
		model.addAttribute("postId", postId);
		
		return "fleaMarket/update";
	}

	
	@GetMapping("/my")
	public String viewMyPage(@RequestParam(value = "view", required = false, defaultValue = "store") String view,
	                         Model model) {
		model.addAttribute("view", view);
		return "fleaMarket/myPosts";
	}
	
}
