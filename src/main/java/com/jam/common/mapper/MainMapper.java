package com.jam.common.mapper;

import java.util.List;

import com.jam.community.dto.CommunityDto;
import com.jam.fleaMarket.dto.FleaMarketDto;


public interface MainMapper {
	List<FleaMarketDto> fleaList();
	List<CommunityDto> comList();
}
