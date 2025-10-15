package com.jam.file.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.s3.service.S3Service;
import com.jam.file.service.FileService;
import com.jam.file.vo.FileAssetVO;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.service.FileAccessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Log4j
public class FileController {
	private final S3Service s3Service;
	private final FileService fileService;
	private final FileAccessService fileAccessService;
	
	@PostMapping("/upload-url")
	public ResponseEntity<Map<String, String>> presignUpload(@RequestBody FileAssetVO file) {
		
		if (file == null || file.getFile_name() == null || file.getFile_type() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "filename 또는 contentType이 누락되었습니다."));
	    }
	    
		try {
	        return ResponseEntity.ok(s3Service.presignUpload(file.getFile_name(), file.getFile_type()));
	    } catch (IllegalArgumentException e) {  
	    	log.error(e.getMessage());
	        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  
	    } catch (Exception e) {        
	    	log.error("error", e);
	        return ResponseEntity.status(500).body(Map.of("error", "서버 오류"));
	    }
	}
	
	/**
	 * 다운로드용 Presigned URL 생성.
	 * 파일 접근 권한 검증 후 S3 객체에 접근 가능한 일시적 다운로드 URL 반환.
	 *
	 * @param fileId 다운로드할 파일의 ID
	 * @param req    HttpServletRequest, JWT 인터셉터에서 설정된 userId 사용
	 * @return 지정된 파일을 다운로드할 수 있는 String Presigned URL 
	 * @throws UnauthorizedException 로그인 되지 않을 시
	 * @throws ForbiddenException    권한없는 파일 요청 시
	 */
	@GetMapping(value="/{fileId}/download-url", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> downloadFile(@PathVariable Long fileId, HttpServletRequest req) {
	    String userId = (String)req.getAttribute("userId");
	    if(userId == null) throw new UnauthorizedException("Unauthorized request: missing userId");
		
	    fileAccessService.existsFileAccess(userId, fileId);
	    
	    FileAssetVO file = fileService.getFileMetaByFileId(fileId);
	    String downloadUrl = s3Service.generatePresignedDownloadUrl(file.getFile_key(), file.getFile_name());
	    
	    return ResponseEntity.ok().body(downloadUrl);
	}
	
}
