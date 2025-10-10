package com.jam.file.controller;

import java.util.Map;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Log4j
public class FileController {
	private final S3Service s3Service;
	
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
}
