package com.example.veyro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
