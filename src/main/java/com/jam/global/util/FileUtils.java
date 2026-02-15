package com.jam.global.util;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.jam.file.dto.FileCategory;
import com.jam.file.dto.FileType;

@Component
public class FileUtils {

	@Value("${file.upload-dir}")
	private String uploadDir;
	
	private static final long MB = 1024L * 1024L;
	private static final long APP_MAX_SIZE = 20L * MB;   // 20MB
	private static final long IMG_MAX_SIZE = 5L * MB;    // 5MB

    public String saveToLocal(MultipartFile file, String postType) {
        try {
            if (file.isEmpty()) return null;

            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String uuid = UUID.randomUUID().toString();
            String savedName = uuid + ext;

            File dir = new File(uploadDir, postType);
            
            if (!dir.exists()) {
                boolean created = dir.mkdirs(); // 폴더 없으면 생성
            	if (!created) {
            		throw new RuntimeException("업로드 디렉토리 생성 실패: " + dir.getAbsolutePath());
            	}
            }
            
            File targetFile = new File(dir, savedName);
            file.transferTo(targetFile); // 파일 저장

            return savedName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void deleteToLocal(String fileName, String postType) {
		if (fileName == null || fileName.isBlank()) {
			return;
		}

		try {
			File dir = new File(uploadDir, postType);
			File filePath = new File(dir, fileName);
			
			if (filePath.exists()) {
				boolean deleted = filePath.delete();
				if (!deleted) {
					// 삭제 실패해도 예외 던지지 않음
					System.err.println("파일 삭제 실패: " + fileName);
				}
			}
		} catch (Exception e) {
			System.err.println("파일 삭제 중 오류 발생: " + fileName);
		}
	}
    
    // 파일 이름 길이 조정
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
	
    // 확장자, mime, 카테고리 모두 일치하는지 확인
    public void validateFileType(String filename, String contentType, FileCategory category) {
    	String ext = extOf(filename);
    	FileType type = FileType.fromExt(ext);
    	String normalized = contentType.split(";")[0].trim().toLowerCase();
    	
    	if (type == null) {
            throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + ext);
        }

        if (!type.mime.equals(normalized)) {
            throw new IllegalArgumentException("확장자와 MIME 유형이 일치하지 않습니다: " + ext + " / " + contentType);
        }

        if (type.category != category) {
            throw new IllegalArgumentException("잘못된 카테고리 업로드입니다: " + type.category + ", "+ category);
        }
    }
    
    // 확장자 추출
	public String extOf(String filename) {
	    int i = filename.lastIndexOf('.');
	    return (i >= 0 && i < filename.length()-1) ? filename.substring(i+1).toLowerCase() : "";
	}
	
	// 파일명 사이즈 확인
	public void validateFileSize(Long fileSize, FileCategory category) {
		switch (category) {
	        case POST_IMAGE:
	            if (fileSize > IMG_MAX_SIZE) // 5MB
	            	throw new IllegalArgumentException("허용된 최대 크기 " + readableMB(IMG_MAX_SIZE) + "를 초과했습니다.(현재 크기: " + readableMB(fileSize) + ")");
	            break;
	
	        case APPLICATION:
	            if (fileSize > APP_MAX_SIZE) // 20MB
	                throw new IllegalArgumentException("허용된 최대 크기 " + readableMB(APP_MAX_SIZE) + "를 초과했습니다.(현재 크기: " + readableMB(fileSize) + ")");
	            break;
	
	        default:
	            // 기본은 20MB 제한
	            if (fileSize > APP_MAX_SIZE)
	            	throw new IllegalArgumentException("허용된 최대 크기 " + readableMB(APP_MAX_SIZE) + "를 초과했습니다.(현재 크기: " + readableMB(fileSize) + ")");
	    }
	}
	
	private static String readableMB(long bytes) {
	    return String.format("%.1fMB", bytes / 1024.0 / 1024.0);
	}
}
