package com.example.veyro.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.User;
import com.example.veyro.service.UserService;

@Controller 
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("admin".equalsIgnoreCase(role)) {
            return "redirect:/admin/dashboard";
        } else if ("kasir".equalsIgnoreCase(role)) {
            return "redirect:/kasir/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("admin".equalsIgnoreCase(role)) {
            return "redirect:/admin/dashboard";
        } else if ("kasir".equalsIgnoreCase(role)) {
            return "redirect:/kasir/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String prosesLogin(@RequestParam String username, @RequestParam String password, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = userService.login(username.trim(), password).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Username atau password salah");
            return "redirect:/login";
        }
        
        String role = (user.getRole() != null) ? user.getRole().trim().toLowerCase() : "";
        if (!"admin".equals(role) && !"kasir".equals(role)) {
            redirectAttributes.addFlashAttribute("pesanError", "Role akun tidak valid");
            return "redirect:/login";
        }
        
        session.setAttribute("user", user);
        session.setAttribute("role", role);
        return "redirect:/" + role + "/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
