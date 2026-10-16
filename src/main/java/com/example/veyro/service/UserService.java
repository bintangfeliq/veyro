package com.example.veyro.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.veyro.model.User;
import com.example.veyro.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User simpanUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> semuaUser() {
        return userRepository.findAll();
    }

    public Optional<User> cariByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> cariById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    public void hapusUser(Long id) {
        userRepository.deleteById(id);
    }
}
