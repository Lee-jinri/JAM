package com.jam.global.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.file.dto.FileAssetDto;
import com.jam.file.service.FileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileReferenceService {
	private final FileService fileService;

	public void insertFiles(List<FileAssetDto> files, Long postId) {
		fileService.insertFiles(files, postId);
	}
	
	public void deleteFiles(FileAssetDto param) {
		fileService.deleteFiles(param);
	}

	public List<FileAssetDto> getFilesByPost(FileAssetDto param) {
		return fileService.getFilesByPost(param);
	}

	public void deleteFilesByKeys(List<String> deleted_keys) {
		fileService.deleteFilesByKeys(deleted_keys);
	}
}
