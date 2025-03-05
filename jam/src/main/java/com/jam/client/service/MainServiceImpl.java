package com.jam.client.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.dao.MainDAO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

@Service
public class MainServiceImpl implements MainService {

	@Autowired
	private MainDAO mainDao;

	@Override
	public List<JobVO> jobList() {
		return mainDao.jobList();
	}

	@Override
	public List<RoomRentalVO> roomList() {
		return mainDao.roomList();
	}

	@Override
	public List<FleaMarketVO> fleaList() {
		return mainDao.fleaList();
	}

	@Override
	public List<CommunityVO> comList() {
		return mainDao.comList();
	}
	

}
