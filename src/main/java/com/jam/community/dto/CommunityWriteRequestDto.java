package com.jam.community.dto;

import java.util.List;

import com.jam.file.dto.FileAssetDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityWriteRequestDto {
    private String title;
    private String content;
    private List<FileAssetDto> fileAssets;
}
