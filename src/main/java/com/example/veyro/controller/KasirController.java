package com.example.veyro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/kasir")
public class KasirController {

    @GetMapping({"/dashboard", "/kasir"})
    public String dashboardKasir(HttpSession session, Model model) {
        if (!"kasir".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        return "kasir/dashboard";
    }

}
