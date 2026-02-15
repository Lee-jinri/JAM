package com.jam.file.service;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jam.file.dto.FileAssetDto;
import com.jam.file.dto.FileCategory;
import com.jam.file.mapper.FileAssetMapper;
import com.jam.global.util.FileUtils;
import com.jam.s3.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

	private final FileUtils fileUtils;
	private final SqlSessionTemplate batchSqlSessionTemplate;
	private final FileAssetMapper fileMapper;
	private final S3Service s3Service;
	
	@Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
	public void insertFiles(List<FileAssetDto> files, Long postId) {
		log.info("files : " +files);
		for (FileAssetDto f : files) {
			String safe = fileUtils.sanitizeFilename(f.getFile_name());

	    	fileUtils.validateFileType(safe, f.getFile_type(), f.getFile_category());
			fileUtils.validateFileSize(f.getFile_size(), f.getFile_category());
			
			f.setFile_name(safe);
			f.setPost_id(postId);
			f.setPost_type(f.getFile_category().toString());

			batchSqlSessionTemplate.insert(
				"com.jam.file.mapper.FileAssetMapper.insertFileAsset", f);
		}
	}
	
	public List<FileAssetDto> getFilesByPost(FileAssetDto file) {
		return fileMapper.getFilesByPost(file);
	}

	public FileAssetDto getFileMetaByFileId(Long fileId) {
		FileAssetDto fileMeta = fileMapper.getFileMetaByFileId(fileId);
		
		if (fileMeta == null || fileMeta.getFile_key() == null || fileMeta.getFile_name() == null) {
	        //throw new NotFoundException("파일 정보를 찾을 수 없습니다.");
	    }
		
		return fileMeta;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteFiles(FileAssetDto param) {
		List<FileAssetDto> files = fileMapper.getFilesByPost(param);
		
		FileCategory type = FileCategory.valueOf(param.getPost_type());
		boolean noFiles = (files == null || files.isEmpty());

		if (type == FileCategory.APPLICATION) {
		    //if (noFiles) throw new NotFoundException("삭제할 파일이 존재하지 않습니다.");
		}

		if (type == FileCategory.POST_IMAGE) {
		    if (noFiles) return;
		}
		
		fileMapper.deleteFiles(files);

		List<String> fileKeys = files.stream().map(FileAssetDto::getFile_key).toList();
		
		try {
			s3Service.deleteObjects(fileKeys);
		} catch (Exception e) {
			log.warn("S3 파일 삭제 실패: fileKeys =" + fileKeys  + " e=" + e.getMessage());
		}
	}

	public void deleteFilesByKeys(List<String> keys) {
		if (keys == null || keys.isEmpty()) return;
		
		for (String key : keys) {
			batchSqlSessionTemplate.delete(
					"com.jam.file.mapper.FileAssetMapper.deleteFilesByKeys", key);
		}
		
		try {
			s3Service.deleteObjects(keys);
		} catch (Exception e) {
			log.warn("S3 파일 삭제 실패: fileKeys =" + keys  + " e=" + e.getMessage());
		}
	}
}
