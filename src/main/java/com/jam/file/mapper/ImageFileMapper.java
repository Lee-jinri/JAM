package com.jam.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.file.dto.ImageFileDto;

public interface ImageFileMapper {
	public List<ImageFileDto> getImages(@Param("post_id") Long post_id, @Param("post_type") String post_type);
	public List<ImageFileDto> findFleaImagesByPostId(@Param("post_id") Long post_id,  @Param("post_type") String post_type);
	ImageFileDto findById(Long imageNo);

	void insertImage(ImageFileDto imageVO);
	void deleteImage(Long imageNo);

	public void clearThumbnailFlag(Long post_id);
	public void setThumbnailFlag(Long resolvedThumbId);
}
