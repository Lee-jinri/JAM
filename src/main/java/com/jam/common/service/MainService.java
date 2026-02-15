package com.jam.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.common.mapper.MainMapper;
import com.jam.community.dto.CommunityDto;
import com.jam.fleaMarket.dto.FleaMarketDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainService {
	
	private final MainMapper mainMapper;
	
	public List<FleaMarketDto> fleaList() {
		return mainMapper.fleaList();
	}

	public List<CommunityDto> comList() {
		return mainMapper.comList();
	}

}
