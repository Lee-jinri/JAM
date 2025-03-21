package com.jam.client.fleaMarket.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.service.FleaMarketService;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/fleaMarket/")
@AllArgsConstructor
@Log4j
public class FleaMarketRestController {

	@Autowired
	private FleaMarketService fleaService;
	
	@GetMapping(value = "boards")
	public ResponseEntity<Map<String, Object>> getBoards(FleaMarketVO flea_vo, HttpServletRequest request){
		try {
			String user_id = (String)request.getAttribute("userId");
			if(user_id != null) flea_vo.setUser_id(user_id);
			
			Map<String, Object> result = new HashMap<>();

			List<FleaMarketVO> fleaMarketList = fleaService.getBoards(flea_vo);
			
			result.put("fleaMarketList", fleaMarketList);
			
			int total = fleaService.listCnt(flea_vo);
			
			PageDTO pageMaker = new PageDTO(flea_vo, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
		}catch(Exception e) {
			log.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
		}
	}

	/**********************************
	 * 중고악기 글 상세정보를 조회하는 메서드입니다.
	 *
	 * @param flea_no 조회할 중고악기 글의 번호
	 * @return ResponseEntity<FleaMarketVO> - 조회된 중고악기 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 * @throws Exception 데이터 조회 중 발생한 예외
	 **************************************/
	@GetMapping(value = "/board/{flea_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FleaMarketVO> getBoardDetail(@PathVariable("flea_no") Long flea_no) throws Exception{
		if (flea_no == null) { 
			log.error("flea_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		try {
	        // 조회수 증가
			fleaService.incrementReadCnt(flea_no);
			
			// 상세 페이지 조회
			FleaMarketVO detail = fleaService.getBoardDetail(flea_no);
	       
	        return new ResponseEntity<>(detail, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	

	/******************************
	 * 중고악기 글을 작성하는 메서드 입니다.
	 * @param FleaMarketVO flea_vo 작성자 id와 닉네임, 제목과 내용, 가격, 카테고리(판매,구매)
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@RequestMapping(value="/board", method=RequestMethod.POST)
	public ResponseEntity<String> writeBoard(@RequestBody FleaMarketVO flea_vo) throws Exception{
		
		String errorMsg;
		if (flea_vo == null) { 
			log.error("flea_vo is null");
			errorMsg = "flea_vo is null.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		// 유효성 검사
		String flea_title = flea_vo.getFlea_title();
		String flea_content = flea_vo.getFlea_content();

		if (flea_title == null) {
			log.error("flea_title is null.");
			errorMsg = "flea_title is null.";
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		if (flea_content == null) {
			log.error("flea_content is null.");
			errorMsg = "flea_content is null.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		try {
			fleaService.writeBoard(flea_vo);
			
			String flea_no = flea_vo.getFlea_no().toString();
			
			return new ResponseEntity<>(flea_no,HttpStatus.OK);
		} catch (Exception e) {
			log.error("중고악기 글 작성 데이터 저장 중 오류 : " + e.getMessage());
			
			String responseBody = e.getMessage();
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	/*******************************************
	 * 중고악기의 수정할 글 정보(제목, 내용, 가격, 카테고리, 거래 완료 여부, 글쓴이 id)를 불러오는 메서드 입니다.
	 * @param flea_no 수정을 위해 불러올 글 번호
	 * @return ResponseEntity<FleaMarketVO> - 조회된 중고악기 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 *******************************************/
	@GetMapping(value = "/board/edit/{flea_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FleaMarketVO> getBoardById(@PathVariable Long flea_no) {
		if (flea_no == null) { 
			log.error("flea_no is required");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}else {
			try {
				FleaMarketVO board = fleaService.getBoardById(flea_no);
			    board.setFlea_no(flea_no);
			    
				return ResponseEntity.ok(board);
			}catch (Exception e) {
				log.error("중고악기 수정 글 불러오던 중 오류 : " + e.getMessage());
				
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
	}
	
	

	/***********************************
	 * 중고악기 글을 수정하는 메서드 입니다.
	 * @param FleaMarketVO flea_vo  작성자 id와 닉네임, 수정할 글 번호, 제목, 내용, 거래 완료 여부, 가격, 카테고리
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 * @return 성공 시 수정한 중고악기 글 상세 페이지 / 실패 시 중고악기 글 수정 페이지
	 ***********************************/
	@RequestMapping(value="/board", method=RequestMethod.PUT)
	public ResponseEntity<String> editBoard(@RequestBody FleaMarketVO flea_vo) throws Exception{
		String errorMsg;
		if (flea_vo == null) { 
			log.error("flea_vo is null");
			errorMsg = "flea_vo is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		// 유효성 검사
		String flea_title = flea_vo.getFlea_title();
		String flea_content = flea_vo.getFlea_content();

		if (flea_title == null) {
			log.error("flea_title is null.");
			errorMsg = "flea_title is null.";
		    
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		if (flea_content == null) {
			log.error("flea_content is null.");
			errorMsg = "flea_content is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		try {
			fleaService.editBoard(flea_vo);
			String flea_no = flea_vo.getFlea_no().toString();
			
			return new ResponseEntity<>(flea_no, HttpStatus.OK);
		} catch(Exception e) {
			log.error("중고악기 editBoard 데이터 수정 중 오류 : " + e.getMessage());
			String responseBody = e.getMessage();
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	

	// 본인인지 확인햐여담
	/**********************************
	 * 중고악기 글을 삭제하는 메서드 입니다.
	 * @param Long flea_no
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@RequestMapping(value="/board", method=RequestMethod.DELETE)
	public ResponseEntity<String> boardDelete(@RequestParam("flea_no") Long flea_no) throws Exception{
		log.info("boardDelete : ");
		
		if (flea_no == null) { 
			log.error("flea_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("flea_no is required");
		}
		
		try {
			fleaService.boardDelete(flea_no);
			
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("중고악기 delete 데이터 삭제 중 오류 : " + e.getMessage());
			
			String resopnseBody = "중고악기 delete 데이터 삭제 중 오류 : " + e.getMessage();
			
			return new ResponseEntity<>(resopnseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("posts")
	public ResponseEntity<Map<String, Object>> posts(@RequestParam(required=false) String user_name, @RequestParam(defaultValue = "1") int page, HttpServletRequest request) throws Exception{
		
		FleaMarketVO flea_vo = new FleaMarketVO();
		
		if(user_name == null || !fleaService.isValidUserName(user_name)) { 
			flea_vo.setUser_id((String) request.getAttribute("userId"));
		}else {
			flea_vo.setUser_id(fleaService.getUserId(user_name));
		}
		
		flea_vo.setPageNum(page);
		
		Map<String, Object> result = new HashMap<>();
		
		List<CommunityVO> posts = fleaService.getPosts(flea_vo);
		result.put("posts", posts);
		
		int total = fleaService.getUserPostCnt(flea_vo);
		
	    PageDTO pageMaker = new PageDTO(flea_vo, total);
	    
	    result.put("pageMaker", pageMaker);
	    
	    return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	
}
