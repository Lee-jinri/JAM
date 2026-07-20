package com.jam.migration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jam.file.dto.ImageFileDto;
import com.jam.file.mapper.ImageFileMapper;

import net.coobird.thumbnailator.Thumbnails;

@Component
public class ThumbnailMigrationRunner implements CommandLineRunner {
	@Autowired
    private ImageFileMapper imageFileMapper;
	
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.thumb-upload-dir}")
    private String thumbUploadDir;
    
    @Value("${migration.thumbnail.enabled:false}")
    private boolean migrationEnabled;

	@Override
	public void run(String... args) throws Exception {
		if (!migrationEnabled) {
            return;
        }

        List<ImageFileDto> allImages = imageFileMapper.findAll(); 

        int success = 0, fail = 0;

        for (ImageFileDto img : allImages) {
            String postType = "flea";
            String fileName = img.getImage_name();

            File originalFile = new File(new File(uploadDir, postType), fileName);
            File thumbDir = new File(thumbUploadDir, postType);
            File thumbFile = new File(thumbDir, fileName);

            if (!originalFile.exists()) {
                System.out.println("원본 없음: " + originalFile.getPath());
                fail++;
                continue;
            }

            if (thumbFile.exists()) {
                continue; 
            }

            try {
                if (!thumbDir.exists()) thumbDir.mkdirs();
                Thumbnails.of(originalFile).size(300, 300).toFile(thumbFile);
                success++;
            } catch (IOException e) {
                System.out.println("실패: " + fileName);
                fail++;
            }
        }

        System.out.println("마이그레이션 완료 - 성공: " + success + ", 실패: " + fail);
	}

}
