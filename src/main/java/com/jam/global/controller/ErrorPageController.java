package com.jam.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorPageController {
	@GetMapping("/error/401")
    public String unauthorizedPage(HttpServletRequest request, Model model) {
        model.addAttribute("message", request.getAttribute("msg"));
        return "error/401";
    }
	
	@GetMapping("/error/403")
    public String accessDeniedPage(HttpServletRequest request, Model model) {
        model.addAttribute("message", request.getAttribute("msg"));
        return "error/403";
    }
}
