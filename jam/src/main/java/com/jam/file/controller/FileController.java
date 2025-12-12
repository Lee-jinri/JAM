package com.jam.file.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.s3.service.S3Service;
import com.jam.file.service.FileService;
import com.jam.file.vo.FileAssetVO;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.service.FileAccessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
	private final S3Service s3Service;
	private final FileService fileService;
	private final FileAccessService fileAccessService;
	
	/**
	 * 업로드용 Presigned URL 생성.
	 * 
	 * @param file 업로드할 파일 정보 (file_name, file_type, file_size, FileCategory)
	 * @return Map:
	 *         - url : 클라이언트가 해당 URL로 PUT 요청을 보내 업로드할 수 있는 presigned URL
	 *         - key : 업로드된 객체가 S3에 저장될 경로(Key)
	 */
	@PostMapping("/upload-url")
	public ResponseEntity<Map<String, String>> presignUpload(@RequestBody FileAssetVO file) {
		
		if (file == null || file.getFile_name() == null || file.getFile_type() == null || file.getFile_size() == null ||file.getFile_category() == null) {
			log.error("presignUpload file 없음. file:"+file);
			throw new BadRequestException("일시적인 오류가 발생했습니다. 잠시 후 다시 시도하세요.");
	    }
		
		return ResponseEntity.ok(s3Service.presignUpload(file.getFile_name(), file.getFile_type(), file.getFile_size(), file.getFile_category()));
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
	    if(userId == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다. 로그인 하시겠습니까?");
		
	    fileAccessService.existsFileAccess(userId, fileId);
	    
	    FileAssetVO file = fileService.getFileMetaByFileId(fileId);
	    String downloadUrl = s3Service.generatePresignedDownloadUrl(file.getFile_key(), file.getFile_name());
	    
	    return ResponseEntity.ok().body(downloadUrl);
	}
	
	/**
	 * S3 객체 조회를 위한 임시 접근 Presigned URL을 생성 후 반환. (24시간)
	 * 
	 * @param key: S3에 저장된 객체의 경로
	 * @return     이미지 조회용 Presigned GET URL
	 */
	@GetMapping("/view-url")
	public ResponseEntity<String> getImageViewUrl(@RequestParam String key) {
		String viewUrl = s3Service.generatePresignedViewUrl(key);

		return ResponseEntity.ok(viewUrl);
	}

	
}
