package com.jam.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.file.dto.FileAssetDto;

public interface FileAssetMapper {
	int insertFileAsset(FileAssetDto vo);
	List<FileAssetDto> getFilesByPost(FileAssetDto file);
	int deleteFiles(List<FileAssetDto> files);
	FileAssetDto getFileMetaByFileId(@Param("file_id") Long fileId);
	void deleteFileByKey(String keys);
}
