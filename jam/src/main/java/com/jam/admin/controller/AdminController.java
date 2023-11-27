package com.jam.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.log4j.Log4j;

@Log4j
@RequestMapping("/admin/*")
@Controller
public class AdminController {
	
	@GetMapping("/admin")
	public String doAdmin() {
		log.info("admin page");
		
		return "/admin/admin";
	}
}
