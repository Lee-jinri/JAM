package com.jam.client.fleaMarket.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jam.client.fleaMarket.service.FleaMarketService;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.common.vo.ImageFileVO;
import com.jam.common.vo.PageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/fleaMarket/")
@RequiredArgsConstructor
@Log4j
public class FleaMarketRestController {

	private final FleaMarketService fleaService;
	
	@GetMapping(value = "board")
	public ResponseEntity<Map<String, Object>> getBoards(FleaMarketVO flea_vo, HttpServletRequest request){
		try {
			String user_id = (String)request.getAttribute("userId");
			if(user_id != null) flea_vo.setUser_id(user_id);
			
			// FIXME: 임시로 해놓음
			flea_vo.setAmount(24);
			
			Map<String, Object> result = new HashMap<>();

			List<FleaMarketVO> fleaMarketList = fleaService.getBoard(flea_vo);
			
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
	@GetMapping(value = "/post/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getBoardDetail(@PathVariable("post_id") Long post_id) throws Exception{
		if (post_id == null) { 
			log.error("post_id is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		try {
			Map<String, Object> result = new HashMap<>();
			
	        // 조회수 증가
			fleaService.incrementReadCnt(post_id);
			
			// 상세 페이지 조회
			FleaMarketVO post = fleaService.getPostDetail(post_id);
			
			// 이미지 파일
			List<ImageFileVO> images = fleaService.getImages(post_id);
			
			result.put("post", post);
			result.put("images", images);
			
	        return new ResponseEntity<>(result, HttpStatus.OK);
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
	@PostMapping(value="/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> writeBoard(
				@RequestParam("title") String title,
		        @RequestParam("content") String content,
		        @RequestParam("price") int price,
		        @RequestParam("category_id") int categoryId,
		        @RequestParam("images") List<MultipartFile> images,
		        HttpServletRequest request
			) throws Exception{
		
		if(request.getAttribute("userId") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		if (title == null || title.trim().isEmpty()) {
		    return ResponseEntity.badRequest().body("제목을 입력하세요.");
		}
		
		if (content == null || content.trim().isEmpty()) {
		    return ResponseEntity.badRequest().body("설명을 입력하세요.");
		}

		
		FleaMarketVO flea_vo = new FleaMarketVO();
		
		flea_vo.setTitle(title);
		flea_vo.setContent(content);
		flea_vo.setPrice(price);
		flea_vo.setCategory_id(categoryId);
		flea_vo.setUser_id((String)request.getAttribute("userId"));
		
		try {
			long postId = fleaService.writePost(flea_vo, images);
			
			return new ResponseEntity<>(Long.toString(postId), HttpStatus.OK);
		} catch (Exception e) {
			log.error("중고악기 작성 데이터 저장 중 오류: " + e.getMessage());
			
			String responseBody = "시스템 오류입니다. 잠시 후 다시 시도해주세요.";
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	/*******************************************
	 * 중고악기의 수정할 글 정보(제목, 내용, 가격, 카테고리, 거래 완료 여부, 글쓴이 id)를 불러오는 메서드 입니다.
	 * @param post_id 수정을 위해 불러올 글 번호
	 * @return ResponseEntity<FleaMarketVO> - 조회된 중고악기 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 *******************************************/
	@GetMapping(value = "/post/edit/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FleaMarketVO> getBoardById(@PathVariable Long post_id) {
		if (post_id == null) { 
			log.error("post_id is required");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}else {
			try {
				FleaMarketVO board = fleaService.getPostForEdit(post_id);
			    board.setPost_id(post_id);
			    
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
	@RequestMapping(value="/post", method=RequestMethod.PUT)
	public ResponseEntity<String> editBoard(@RequestBody FleaMarketVO flea_vo) throws Exception{
		String errorMsg;
		if (flea_vo == null) { 
			log.error("flea_vo is null");
			errorMsg = "flea_vo is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		// 유효성 검사
		String title = flea_vo.getTitle();
		String content = flea_vo.getContent();

		if (title == null) {
			log.error("flea_title is null.");
			errorMsg = "flea_title is null.";
		    
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		if (content == null) {
			log.error("flea_content is null.");
			errorMsg = "flea_content is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		try {
			fleaService.editPost(flea_vo);
			String post_id = flea_vo.getPost_id().toString();
			
			return new ResponseEntity<>(post_id, HttpStatus.OK);
		} catch(Exception e) {
			log.error("중고악기 editBoard 데이터 수정 중 오류 : " + e.getMessage());
			String responseBody = e.getMessage();
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	/**********************************
	 * 중고악기 글을 삭제하는 메서드 입니다.
	 * @param Long flea_no
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@RequestMapping(value="/post", method=RequestMethod.DELETE)
	public ResponseEntity<String> boardDelete(@RequestParam("post_id") Long post_id, HttpServletRequest request) throws Exception{
		
		if (post_id == null) { 
			log.error("flea_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("postId is required");
		}
		
		try {
			String userId = (String)request.getAttribute("userId");
			
			if(userId == null) {
				log.error("Unauthorized.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}
			
			fleaService.deletePost(post_id, userId);
			
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("중고악기 delete 데이터 삭제 중 오류 : " + e.getMessage());
			
			String resopnseBody = "중고악기 delete 데이터 삭제 중 오류 : " + e.getMessage();
			
			return new ResponseEntity<>(resopnseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/********************************
	 * 중고악기 사진 업로드
	 * @return String 사진 저장 경로 
	 ********************************/
	@PostMapping(value="/uploadImageFile", produces = "application/json; charset=utf8")
	public ResponseEntity<Map<String, Object>> uploadImageFile(
			@RequestParam("file") MultipartFile multipartFile, 
			HttpServletRequest request)  {
		
		Map<String, Object> response = new HashMap<>();
				
		// 내부경로로 저장
		String contextRoot = new HttpServletRequestWrapper(request).getRealPath("/");
		String fileRoot = contextRoot+"resources/fileupload/";
		
		String originalFileName = multipartFile.getOriginalFilename();	//오리지날 파일명
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));	//파일 확장자
		String savedFileName = UUID.randomUUID() + extension;	//저장될 파일 명
		
		File targetFile = new File(fileRoot + savedFileName);	
		
		try {
			InputStream fileStream = multipartFile.getInputStream();
			FileUtils.copyInputStreamToFile(fileStream, targetFile);	//파일 저장
			response.put("url", "/resources/fileupload/" + savedFileName);
	        response.put("responseCode", "success");
	        
	        return ResponseEntity.ok(response);
	        /*
			jsonObject.addProperty("url", "/resources/fileupload/"+savedFileName); // contextroot + resources + 저장할 내부 폴더명
			jsonObject.addProperty("responseCode", "success");*/
				
		} catch (IOException e) {
			FileUtils.deleteQuietly(targetFile);	//저장된 파일 삭제
			log.error(e.getMessage());
			
			response.put("responseCode", "error")
			;
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	@GetMapping(value="/my/store")
	public ResponseEntity<Map<String, Object>> getMyStore(FleaMarketVO flea, HttpServletRequest request){
		try {
			String user_id = (String)request.getAttribute("userId");
			
			if(user_id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			flea.setUser_id(user_id);
			// FIXME: 임시
			flea.setAmount(36);
			
			Map<String, Object> result = new HashMap<>();

			List<FleaMarketVO> myStoreList = fleaService.getMyStore(flea);
			
			result.put("fleaMarketList", myStoreList);
			result.put("userName", request.getSession().getAttribute("userName"));
			
			int total = fleaService.getMyStoreCnt(flea);
			
			PageDTO pageMaker = new PageDTO(flea, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
		}catch(Exception e) {
			log.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
		}
	}
	
	@GetMapping("/my/favorites")
	public ResponseEntity<Map<String, Object>> favorites(FleaMarketVO flea, HttpServletRequest request){
		try {
			String user_id = (String)request.getAttribute("userId");
			
			if(user_id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			
			flea.setUser_id(user_id);
			// FIXME: 임시
			flea.setAmount(36);
			
			Map<String, Object> result = new HashMap<>();

			List<FleaMarketVO> favoriteList = fleaService.getFavorites(flea);
			
			result.put("fleaMarketList", favoriteList);
			result.put("userName", request.getSession().getAttribute("userName"));
			
			int total = fleaService.getMyStoreCnt(flea);
			
			PageDTO pageMaker = new PageDTO(flea, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
			
		}catch(Exception e) {
			log.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
		}
		
	}
}
