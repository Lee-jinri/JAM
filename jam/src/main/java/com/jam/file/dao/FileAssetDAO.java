package com.jam.file.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.jam.file.vo.FileAssetVO;

public interface FileAssetDAO {
	int insertFileAsset(FileAssetVO vo);
	List<FileAssetVO> getFileByApplicationId(FileAssetVO file);
	FileAssetVO getFileMetaByFileId(@Param("file_id") Long fileId);
}
