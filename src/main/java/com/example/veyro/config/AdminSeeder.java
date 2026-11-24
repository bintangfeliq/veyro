package com.example.veyro.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.veyro.model.User;
import com.example.veyro.service.UserService;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner isiAdminDefault(UserService userService) {
        return args -> {
            sinkronkanAdmin(userService, "Admin Veyro", "admin.veyro", "veryo.password");
            sinkronkanAdmin(userService, "Bintang Admin", "bintang.admin", "admin.veyro");
        };
    }

    private void sinkronkanAdmin(UserService userService, String name, String username, String password) {
        User user = userService.cariByUsername(username).orElse(null);
        if (user == null) {
            user = new User();
            user.setName(name);
        } else if (user.getName() == null || user.getName().isBlank()) {
            user.setName(name);
        }
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("admin");
        userService.simpanUser(user);
    }
}