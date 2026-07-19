package com.jam.community.dto;

import java.util.List;

import com.jam.file.dto.FileAssetDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommunityEditRequestDto {
	private String title;
    private String content;
	private List<FileAssetDto> file_assets;
	private List<String> deleted_keys;
}
