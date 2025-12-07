package com.jam.file.dao;

import com.jam.file.vo.ImageFileVO;

public interface ImageFileDAO {
	void insertImage(ImageFileVO imageVO);
	void deleteImage(Long imageNo);
	String findNameById(Long imageNo);
}
