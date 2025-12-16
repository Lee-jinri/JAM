package com.jam.file.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.file.vo.ImageFileVO;

public interface ImageFileDAO {
	public List<ImageFileVO> getImages(@Param("post_id") Long post_id, @Param("post_type") String post_type);
	void insertImage(ImageFileVO imageVO);
	void deleteImage(Long imageNo);
	String findNameById(Long imageNo);
}
