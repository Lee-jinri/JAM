package com.jam.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/common")
@Slf4j
public class CommonController {
	
	/*****************
	 * 
	 */
	@GetMapping("/unauthorized")
	public String unauthorized() {
		return "/common/unauthorized";
	}
	
	@GetMapping("/accessDenied")
	public String accessDenied() {
		return "/common/accessDenied";
	}
	
	
	
}
