package com.jam.fleaMarket.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jam.common.dto.PageDto;
import com.jam.file.dto.ImageFileDto;
import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.fleaMarket.service.FleaMarketService;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.util.HtmlSanitizer;
import com.jam.global.util.ValueUtils;
import com.jam.member.dto.MemberDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/fleaMarket/")
@RequiredArgsConstructor
@Slf4j
public class FleaMarketRestController {
	private final FleaMarketService fleaService;
	
	@GetMapping(value = "board")
	public ResponseEntity<Map<String, Object>> getBoards(
			FleaMarketDto flea, 
			HttpServletRequest request,
			@AuthenticationPrincipal MemberDto user){
		
		if (user != null) {
			flea.setUser_id(user.getUser_id());
		}
		
		String kw = flea.getKeyword();
		flea.setKeyword(ValueUtils.sanitizeForLike(kw));
		
		// FIXME: 임시
		flea.setAmount(24);
		
		Map<String, Object> result = new HashMap<>();

		List<FleaMarketDto> fleaMarketList = fleaService.getBoard(flea);
		
		result.put("fleaMarketList", fleaMarketList);
		
		int total = fleaService.listCnt(flea);
		
		PageDto pageMaker = new PageDto(flea, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}

	/**********************************
	 * 중고악기 글 상세정보를 조회하는 메서드입니다.
	 * 
	 * @param post_id 조회할 게시글 번호
     * @param user 현재 접속 중인 인증된 사용자 정보 (Spring Security)
	 * @return ResponseEntity<Map<String, Object>> - 게시글 정보, 이미지 목록, 작성자 여부를 포함한 응답
	 **************************************/
	@GetMapping(value = "/post/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getBoardDetail(@PathVariable("post_id") Long post_id, @AuthenticationPrincipal MemberDto user) {
		if (post_id == null) { 
			log.error("post_id is required");
			throw new BadRequestException("잘못된 요청입니다. 잠시 후 다시 시도하세요.");
		}
		
		Map<String, Object> result = new HashMap<>();

        // 조회수 증가
		fleaService.incrementReadCnt(post_id);
		
		// 상세 페이지 조회
		FleaMarketDto flea = new FleaMarketDto();
		if(user != null) flea.setUser_id(user.getUser_id());
		flea.setPost_id(post_id);
		
		FleaMarketDto post = fleaService.getPostDetail(flea);
		
		// 이미지 파일
		List<ImageFileDto> images = fleaService.getImages(post_id);

		result.put("post", post);
		result.put("images", images);
		
		boolean isAuthor = user != null && user.getUser_id() != null && user.getUser_id().equals(post.getUser_id());
		result.put("isAuthor", isAuthor);
		
        return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	/******************************
	 * 중고악기 글을 작성하는 메서드 입니다.
	 * @param FleaMarketDto flea 작성자 id와 닉네임, 제목과 내용, 가격, 카테고리(판매,구매)
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@PreAuthorize("isAuthenticated()")
	@PostMapping(value="/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> writeBoard(
				@RequestParam("title") String title,
		        @RequestParam("content") String content,
		        @RequestParam("price") int price,
		        @RequestParam("category_id") int categoryId,
		        @RequestParam("images") List<MultipartFile> images,
		        @AuthenticationPrincipal MemberDto user
			){
		
		if(user == null || user.getUser_id().isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		if (title == null || title.trim().isEmpty()) {
		    return ResponseEntity.badRequest().body("제목을 입력하세요.");
		}
		
		if (content == null || content.trim().isEmpty()) {
		    return ResponseEntity.badRequest().body("설명을 입력하세요.");
		}
		if (images == null || images.isEmpty()) {
			return ResponseEntity.badRequest().body("사진은 최소 1장 이상 등록해야 합니다.");
		}
		if (price <= 0) {
			return ResponseEntity.badRequest().body("가격은 0원보다 커야 합니다.");
		}
		title = HtmlSanitizer.sanitizeTitle(title);
		content = HtmlSanitizer.sanitizeHtml(content);
		
		FleaMarketDto flea = new FleaMarketDto();
		flea.setTitle(title);
		flea.setContent(content);
		flea.setPrice(price);
		flea.setCategory_id(categoryId);
		flea.setUser_id(user.getUser_id());
		
		long postId = fleaService.writePost(flea, images);
		
		return new ResponseEntity<>(Long.toString(postId), HttpStatus.OK);
	}
	

	/*******************************************
	 * 중고악기의 수정할 글 정보(제목, 내용, 가격, 카테고리, 거래 완료 여부, 글쓴이 id)를 불러오는 메서드 입니다.
	 * @param post_id 수정을 위해 불러올 글 번호
	 * @return ResponseEntity<FleaMarketDto> - 조회된 중고악기 글의 정보와 HTTP 상태 코드를 포함한 응답 Dto
	 *******************************************/
	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/posts/{post_id}/edit-data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getEditData(
			@PathVariable("post_id") Long post_id) {

		Map<String, Object> result = new HashMap<>();

		FleaMarketDto post = fleaService.getPostForEdit(post_id);

		if (post == null) {
		    throw new NotFoundException("존재하지 않는 게시글입니다.");
		}
		// 이미지 파일
		List<ImageFileDto> images = fleaService.getImages(post_id);
		
		result.put("post", post);
		result.put("images", images);
		
        return new ResponseEntity<>(result, HttpStatus.OK);
	}
	

	/***********************************
	 * 중고악기 글을 수정하는 메서드 입니다.
	 * @param FleaMarketDto flea  작성자 id와 닉네임, 수정할 글 번호, 제목, 내용, 거래 완료 여부, 가격, 카테고리
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 * @return 성공 시 수정한 중고악기 글 상세 페이지 / 실패 시 중고악기 글 수정 페이지
	 ***********************************/
	@PreAuthorize("isAuthenticated()")
	@PostMapping(value = "/post/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> editBoard(
			@RequestParam("postId") Long postId,
			@RequestParam("title") String title,
			@RequestParam("content") String content,
			@RequestParam("price") int price,
			@RequestParam("category_id") int categoryId,
			@RequestParam(value = "thumbnailId", required = false) Long thumbnailId, 
	        @RequestParam(value = "thumbnailName", required = false) String thumbnailName,
			@RequestParam(value = "newImages", required = false) List<MultipartFile> images,
			@RequestParam(value = "deletedImages", required = false) List<Long> deletedImages,
	        @AuthenticationPrincipal MemberDto user) {
		
		if(user == null || user.getUser_id().isEmpty()) {
			throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		}
		
		if (postId == null) {
			throw new BadRequestException("시스템 오류입니다. 잠시 후 다시 시도하세요.");
		}
		
		if (title == null || title.trim().isEmpty()) {
			throw new BadRequestException("제목을 입력하세요.");
		}
		
		if (content == null || content.trim().isEmpty()) {
			throw new BadRequestException("설명을 입력하세요.");
		}
		
	    if (thumbnailId == null && thumbnailName == null) {
	        throw new RuntimeException("썸네일이 설정되지 않았습니다.");
	    }
	    
		if (price <= 0) {
			return ResponseEntity.badRequest().body("가격은 0원보다 커야 합니다.");
		}

		title = HtmlSanitizer.sanitizeTitle(title);
		content = HtmlSanitizer.sanitizeHtml(content);
		
		FleaMarketDto flea = new FleaMarketDto();
		flea.setPost_id(postId);
		flea.setTitle(title);
		flea.setContent(content);
		flea.setPrice(price);
		flea.setCategory_id(categoryId);
		flea.setUser_id(user.getUser_id());
		
		fleaService.editPost(flea, images, deletedImages, thumbnailId, thumbnailName);
		
		return new ResponseEntity<>(postId.toString(), HttpStatus.OK);	
	}
	
	/**********************************
	 * 중고악기 글을 삭제하는 메서드 입니다.
	 * @param Long post_id
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/post/{post_id}")
	public ResponseEntity<String> boardDelete(@PathVariable("post_id") Long post_id, @AuthenticationPrincipal MemberDto user) throws Exception{
		
		if (post_id == null) { 
			log.error("post_id is required");
			throw new BadRequestException("시스템 오류입니다. 잠시 후 다시 시도하세요");
		}
		
		if(user == null || user.getUser_id().isEmpty()) {
			throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
		
		fleaService.deletePost(post_id, user.getUser_id());
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/********************************
	 * 중고악기 사진 업로드
	 * @return String 사진 저장 경로 
	 ********************************/
	@PreAuthorize("isAuthenticated()")
	@PostMapping(value="/uploadImageFile", produces = "application/json; charset=utf8")
	public ResponseEntity<Map<String, Object>> uploadImageFile(
			@RequestParam("file") MultipartFile multipartFile, 
			HttpServletRequest request)  {
		
		Map<String, Object> response = new HashMap<>();
				
		// 내부경로로 저장
		String contextRoot = request.getServletContext().getRealPath("/");
		//String contextRoot = new HttpServletRequestWrapper(request).getRealPath("/");
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
	
	/**
	 * 본인이 작성한 중고악기 게시글을 조회하는 메서드입니다.
	 * 
	 * @param pageNum	요청한 페이지 번호
	 * @param keyword	검색 키워드 (없을 경우 전체 조회)
	 * @param user		현재 로그인한 사용자 정보
	 * 
	 * @return HttpStatus.OK와 게시글 리스트를 반환
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping(value="/my/store")
	public ResponseEntity<Map<String, Object>> getMyStore(FleaMarketDto flea, @AuthenticationPrincipal MemberDto user){
		
		if(user == null || user.getUser_id().isEmpty()) throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		flea.setUser_id(user.getUser_id());
		// FIXME: 임시
		flea.setAmount(36);
		
		Map<String, Object> result = new HashMap<>();

		List<FleaMarketDto> myStoreList = fleaService.getMyStore(flea);
		
		result.put("fleaMarketList", myStoreList);
		
		int total = fleaService.getMyStoreCnt(flea);
		
		PageDto pageMaker = new PageDto(flea, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}

	/**
	 * 중고악기 찜한 글을 조회하는 메서드 입니다.
	 * 
	 * @param user		현재 로그인한 사용자 정보
	 * @param pageNum	요청한 페이지 번호
	 *
	 * @return HttpStatus.OK와 찜한 글 리스트를 반환
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/favorites")
	public ResponseEntity<Map<String, Object>> favorites(FleaMarketDto flea, @AuthenticationPrincipal MemberDto user){
		
		if(user == null || user.getUser_id().isEmpty()) throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		
		flea.setUser_id(user.getUser_id());
		// FIXME: 임시
		flea.setAmount(36);
		
		Map<String, Object> result = new HashMap<>();

		List<FleaMarketDto> favoriteList = fleaService.getFavorites(flea);
		
		result.put("fleaMarketList", favoriteList);
		
		int total = fleaService.getMyStoreCnt(flea);
		
		PageDto pageMaker = new PageDto(flea, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}
}
