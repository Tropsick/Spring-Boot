package com.example.demo.services;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        user.setKarma(20); // Устанавливаем карму по умолчанию
        // Хэшируем пароль перед сохранением
        user.setPasswordHash(passwordEncoder.encode(user.getPassword())); // Используем поле user.getPassword()
        userRepository.save(user);
    }


    public void updateAvatar(String username, String avatar) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        existingUser.ifPresent(user -> {
            user.setAvatar(avatar);
            userRepository.save(user);
        });
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
