package com.jam.global.util;

import java.io.File;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUtils {

	private final String uploadDir = "C:/upload"; 

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
}
