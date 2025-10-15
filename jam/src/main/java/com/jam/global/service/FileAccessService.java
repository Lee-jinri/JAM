package com.jam.global.service;

import org.springframework.stereotype.Service;

import com.jam.global.exception.ForbiddenException;
import com.jam.global.mapper.FileAccessMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileAccessService {
	private final FileAccessMapper fileAccessMapper;
	
	public boolean existsFileAccess(String userId, Long fileId) {
		
		int access = fileAccessMapper.existsFileAccess(userId, fileId);
	    if(access == 0) throw new ForbiddenException("다운로드 할 권한이 없습니다.");
		
		return true;
	}
}
