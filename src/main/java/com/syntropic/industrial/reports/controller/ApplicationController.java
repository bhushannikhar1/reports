package com.syntropic.industrial.reports.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {

    @Value("${appmode:sqlserver}")
    private String appMode;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mode", appMode.toUpperCase());
        return "index";
    }

}
