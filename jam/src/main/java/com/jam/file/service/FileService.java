package com.jam.file.service;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.s3.service.S3Service;
import com.jam.file.dao.FileAssetDAO;
import com.jam.file.vo.FileAssetVO;
import com.jam.global.exception.NotFoundException;
import com.jam.global.util.FileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@RequiredArgsConstructor
@Log4j
public class FileService {
	
	private final FileUtils fileUtils;
	private final SqlSessionTemplate batchSqlSessionTemplate;
	private final FileAssetDAO fileDao;
	private final S3Service s3Service;
	
	@Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
	public void insertFiles(List<FileAssetVO> files, Long application_id) {
		for (FileAssetVO f : files) {
			String safe = fileUtils.sanitizeFilename(f.getFile_name());
			String ct = fileUtils.normalizeContentType(f.getFile_type(), safe);
			fileUtils.validateFilename(safe);
			fileUtils.validateFileSize(f.getFile_size());
			
			f.setFile_name(safe);
			f.setFile_type(ct);
			f.setPost_type("APPLICATION");
			f.setPost_id(application_id);

			batchSqlSessionTemplate.insert(
				"com.jam.file.dao.FileAssetDAO.insertFileAsset", f);
		}
	}
	
	public List<FileAssetVO> getFilesByPost(FileAssetVO file) {
		return fileDao.getFilesByPost(file);
	}

	public FileAssetVO getFileMetaByFileId(Long fileId) {
		FileAssetVO fileMeta = fileDao.getFileMetaByFileId(fileId);
		
		if (fileMeta == null || fileMeta.getFile_key() == null || fileMeta.getFile_name() == null) {
	        throw new NotFoundException("파일 정보를 찾을 수 없습니다.");
	    }
		
		return fileMeta;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteFiles(FileAssetVO param) {
		List<FileAssetVO> files = fileDao.getFilesByPost(param);
		
		if (files == null || files.isEmpty()) {
			throw new NotFoundException("삭제할 파일이 존재하지 않습니다.");
		}
		
		fileDao.deleteFiles(files);

		List<String> fileKeys = files.stream().map(FileAssetVO::getFile_key).toList();
		try {
			s3Service.deleteObjects(fileKeys);
		} catch (Exception e) {
			log.warn("S3 파일 삭제 실패: fileKeys =" + fileKeys  + " e=" + e.getMessage());
		}
	}

	
}
