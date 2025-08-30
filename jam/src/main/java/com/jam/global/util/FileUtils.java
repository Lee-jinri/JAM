package com.jam.global.util;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUtils {

	private final String uploadDir = "C:/upload"; 

	private static final Set<String> ALLOWED_EXTS = Set.of("pdf","doc","docx","hwp");
	private static final Set<String> ALLOWED_MIME = Set.of(
	    "application/pdf",
	    "application/msword",
	    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
	    "application/x-hwp", "application/haansofthwp" 
	);
	
	private static final long MAX_SIZE_BYTES = 20L * 1024 * 1024; // 20MB

    public String saveToLocal(MultipartFile file, String postType) {
        try {
            if (file.isEmpty()) return null;

            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String uuid = UUID.randomUUID().toString();
            String savedName = uuid + ext;

            File dir = new File(uploadDir, postType);
            
            if (!dir.exists()) {
                dir.mkdirs(); // 폴더 없으면 생성
            }
            
            File targetFile = new File(dir, savedName);
            file.transferTo(targetFile); // 파일 저장

            return savedName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    

    public String sanitizeFilename(String filename) {
	    if (filename == null) throw new IllegalArgumentException("filename required");
	    String f = filename.trim();

	    // 경로 분리자 제거
	    f = f.replaceAll("[\\\\/]+", "_");

	    // 제어문자 제거
	    f = f.replaceAll("\\p{Cntrl}", "");

	    // 너무 길면 뒤쪽만 남김 (확장자는 보존)
	    int max = 200;
	    int dot = f.lastIndexOf('.');
	    if (f.length() > max) {
	        if (dot > 0) {
	            String name = f.substring(0, dot);
	            String ext  = f.substring(dot);
	            if (name.length() > (max - ext.length())) {
	                name = name.substring(name.length() - (max - ext.length()));
	            }
	            f = name + ext;
	        } else {
	            f = f.substring(f.length() - max);
	        }
	    }

	    if (f.isBlank()) f = "file";

	    return f;
	}
	

	public void validateFilename(String filename) {
	    String ext = extOf(filename);
	    
	    if (!ALLOWED_EXTS.contains(ext)) {
	        throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + ext);
	    }
	    
	    // 이중 확장자 방지 (.pdf.exe)
	    String lower = filename.toLowerCase();
	    int firstDot = lower.indexOf('.');
	    int lastDot  = lower.lastIndexOf('.');
	    if (firstDot != -1 && firstDot != lastDot) {
	        // 필요 시 더 강하게 막고 싶으면 여기서도 거절
	    }
	}

	public String normalizeContentType(String contentType, String filename) {
		
	    if (contentType == null || contentType.isBlank()) {
	        switch (extOf(filename)) {
	            case "pdf":  return "application/pdf";
	            case "doc":  return "application/msword"; 
	            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	            case "hwp":  return "application/x-hwp";
	        }
	        return "application/octet-stream";
	    }
	    
	    String ct = contentType.toLowerCase();
	    
	    if (!ALLOWED_MIME.contains(ct)) {
	        return normalizeContentType("", filename);
	    }
	    
	    return ct;
	}
	
	
	public String extOf(String filename) {
	    int i = filename.lastIndexOf('.');
	    return (i >= 0 && i < filename.length()-1) ? filename.substring(i+1).toLowerCase() : "";
	}
	
	public void validateFileSize(Long fileSize) {
		if (fileSize > MAX_SIZE_BYTES) {
			throw new IllegalArgumentException(
				"허용된 최대 크기(" + (MAX_SIZE_BYTES / (1024 * 1024)) + "MB)을 초과했습니다.");
		}
	}
}
