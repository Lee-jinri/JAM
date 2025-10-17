package com.jam.global.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.file.service.FileService;
import com.jam.file.vo.FileAssetVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileReferenceService {
	private final FileService fileService;

	public void deleteFiles(FileAssetVO param) {
		fileService.deleteFiles(param);
	}

	public List<FileAssetVO> getFilesByPost(FileAssetVO param) {
		return fileService.getFilesByPost(param);
	}
}
