package com.example.veyro.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.User;
import com.example.veyro.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
 
    @PostMapping("/simpan")
    @ResponseBody
    public String simpanUser(@RequestParam String name, @RequestParam String username, @RequestParam String password) {
        if (name.isBlank() || username.isBlank() || password.isBlank()) {
            return "Semua data wajib diisi";
        }
        if (userService.cariByUsername(username.trim()).isPresent()) {
            return "Username sudah digunakan";
        }
        User user = new User();
        user.setName(name.trim());
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setRole("kasir");
        userService.simpanUser(user);
        return "Akun kasir berhasil dibuat"; 
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = userService.login(username.trim(), password).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Username atau password salah");
            return "redirect:/login";
        }
        if (!"admin".equals(user.getRole()) && !"kasir".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("pesanError", "Role akun tidak valid");
            return "redirect:/login";
        }
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        return "redirect:/" + user.getRole() + "/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}