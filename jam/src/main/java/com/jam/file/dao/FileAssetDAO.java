package com.jam.file.dao;

import java.util.List;

import com.jam.file.vo.FileAssetVO;

public interface FileAssetDAO {
	int insertFileAsset(FileAssetVO vo);
	List<FileAssetVO> getFileByApplicationId(FileAssetVO file);
}
