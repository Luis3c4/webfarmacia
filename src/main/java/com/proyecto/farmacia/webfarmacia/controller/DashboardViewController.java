package com.proyecto.farmacia.webfarmacia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardViewController {
    
    @GetMapping("/home")
    public String getHome() {
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String getDashboard() {
        return "index";
    }
} 