package com.jam.file.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.s3.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
@RestController
@RequestMapping("/api/files/")
@RequiredArgsConstructor
@Log4j
public class FileController {
	private final S3Service s3Service;
	
	@GetMapping("/s3/presign/upload")
	public ResponseEntity<Map<String, String>> presignUpload(
			@RequestParam String filename, @RequestParam String contentType) {
		
		try {
	        return ResponseEntity.ok(
	        		s3Service.presignUpload(filename, contentType));
	    } catch (IllegalArgumentException e) {  
	    	log.error(e.getMessage());
	        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));  
	    } catch (Exception e) {        
	    	log.error("error", e);
	        return ResponseEntity.status(500).body(Map.of("error", "서버 오류"));
	    }
	}
}
