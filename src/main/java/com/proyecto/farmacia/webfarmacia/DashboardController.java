package com.proyecto.farmacia.webfarmacia;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {
    @GetMapping("/home")
    public String getMethodName() {
        return "index";
    }
}
