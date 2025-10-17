package com.jam.client.s3.service;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import com.jam.global.util.FileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Log4j
public class S3Service {
	
	private final FileUtils fileUtils;
	private final S3Presigner presigner;
	private final S3Client s3Client;
	
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public Map<String, String> presignUpload(String filename, String contentType) {
		String safe = fileUtils.sanitizeFilename(filename);
		String ct = fileUtils.normalizeContentType(contentType, safe);
		fileUtils.validateFilename(safe);
		
	    Map<String,String> res = generatePresignedUploadUrl(safe, ct);
	    
	    res.put("contentType", ct); 
	    res.put("filename", safe);
	    
	    return res;
	}

	/**
	 * 업로드용 Presigned URL 생성.
	 * @param bucket: S3 버킷 이름
	 * @param key: 저장될 객체 경로
	 * @param contentType: 파일 MIME 타입 (application/pdf, image/png 등)
	 * @return Map:
	 *         - url : 클라이언트가 해당 URL로 PUT 요청을 보내 업로드할 수 있는 presigned URL
	 *         - key : 업로드된 객체가 S3에 저장될 경로(Key)
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
	
	/**
	 * 다운로드용 Presigned URL 생성.
	 * @param key		S3에 저장된 객체 키 (저장 경로)
	 * @param fileName 	사용자에게 다운로드 시 노출할 파일명
	 * @return 클라이언트가 해당 URL로 접근하면 지정된 파일을 다운로드할 수 있는 Presigned URL 문자열
	 */
	public String generatePresignedDownloadUrl(String key, String fileName) {
	    
	    String safe = fileName == null ? "file" : fileName.replace("\"", "");
	    String encoded = UriUtils.encode(safe, StandardCharsets.UTF_8);

	    GetObjectRequest getReq = GetObjectRequest.builder()
	            .bucket(bucket)
	            .key(key)
	            .responseContentDisposition(
	                "attachment; filename=\"download\"; filename*=UTF-8''" + encoded
	            )
	            .build();

	    GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
	            .getObjectRequest(getReq)
	            .signatureDuration(Duration.ofMinutes(5)) 
	            .build();

	    PresignedGetObjectRequest presigned = presigner.presignGetObject(presignReq);
	    return presigned.url().toString();
	}

	private String buildKey(String filename) {
		LocalDate today = LocalDate.now();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return String.format("applications/%s/%s/%s",
			today, uuid, filename);
	}
	
	/**
	 * S3에 저장된 파일들을 삭제합니다.
	 *
	 * @param fileKeys 삭제할 파일의 S3 key 목록
	 * 
	 * @implNote
	 *  - 파일이 존재하지 않아도 예외 없이 무시합니다.
	 *  - 일부 파일의 삭제를 실패해도 나머지 삭제 작업은 계속 진행됩니다.
	 */
	public void deleteObjects(List<String> fileKeys) {
		if (fileKeys == null || fileKeys.isEmpty()) return;
		
		for (String key : fileKeys) {
			try {
				s3Client.deleteObject(DeleteObjectRequest.builder()
					.bucket(bucket)
					.key(key)
					.build());
			} catch (S3Exception e) {
				log.warn("S3 파일 삭제 실패: key=" + key + ", err=" + e.getMessage());
			}
		}
	}

}
