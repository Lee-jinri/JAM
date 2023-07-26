package com.jam.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;
import com.jam.client.service.MainService;

@Controller
public class MainController {
	
	@Autowired
	private MainService mainService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
		
		List<JobVO> jobList = mainService.jobList();
		List<RoomRentalVO> roomList = mainService.roomList();
		List<FleaMarketVO> fleaList = mainService.fleaList();
		List<CommunityVO> comList = mainService.comList();
		
		model.addAttribute("jobList", jobList);
		model.addAttribute("roomList", roomList);
		model.addAttribute("fleaList", fleaList);
		model.addAttribute("comList", comList);
		
		return "main";
	}
}
