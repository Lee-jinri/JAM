package com.jam.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.file.dto.ImageFileDto;

public interface ImageFileMapper {
	public List<ImageFileDto> getImages(@Param("post_id") Long post_id, @Param("post_type") String post_type);
	void insertImage(ImageFileDto imageVO);
	void deleteImage(Long imageNo);
	String findNameById(Long imageNo);
}
