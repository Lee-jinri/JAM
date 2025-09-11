package com.jam.client.s3.service;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jam.global.util.FileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Log4j
public class S3Service {
	
	private final FileUtils fileUtils;
	private final S3Presigner presigner;
	
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	

	/**
	 * 업로드용 Presigned URL 생성.
	 * @param bucket: S3 버킷 이름
	 * @param key: 저장될 객체 경로
	 * @param contentType: 파일 MIME 타입 (application/pdf, image/png 등)
	 * @return
	 */
	public Map<String, String> generatePresignedUploadUrl(String filename, String contentType) {
		String key = buildKey(filename);
		
		var req = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType(contentType)
			.build();

		var presignReq = PutObjectPresignRequest.builder()
			.putObjectRequest(req)
			.signatureDuration(Duration.ofMinutes(5)) //URL 유효 시간 = 5분
			.build();

		URL url = presigner.presignPutObject(presignReq).url();
		
		Map<String, String> map = new HashMap<>();
		map.put("url", url.toString());
		map.put("key", key);
		
		return map;
	}
	
	public Map<String, String> presignUpload(String filename, String contentType) {
		String safe = fileUtils.sanitizeFilename(filename);
		String ct = fileUtils.normalizeContentType(contentType, safe);
		fileUtils.validateFilename(safe);
		
        Map<String,String> res = generatePresignedUploadUrl(safe, ct);
        
        res.put("contentType", ct); 
        res.put("filename", safe);
        
        return res;
	}
	
	private String buildKey(String filename) {
		LocalDate today = LocalDate.now();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return String.format("applications/%s/%s/%s",
			today, uuid, filename);
	}
	

}
