package com.jam.global.mapper;

import org.apache.ibatis.annotations.Param;

public interface FileAccessMapper {
	int existsFileAccess(@Param("user_id") String userId, @Param("file_id") Long fileId);
}
