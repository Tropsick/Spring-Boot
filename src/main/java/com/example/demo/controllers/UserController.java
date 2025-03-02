package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("Пользователь успешно зарегистрирован!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    @PostMapping("/updateAvatar")
    public ResponseEntity<String> updateAvatar(@RequestBody User user) {
        Optional<User> existingUser = userService.findByUsername(user.getUsername());
        if (existingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        userService.updateAvatar(user.getUsername(), user.getAvatar());
        return ResponseEntity.ok("Аватар успешно обновлен!");
    }

    @GetMapping("/getAvatar")
    public ResponseEntity<Map<String, String>> getAvatar(@RequestParam String username) {
        Optional<User> user = userService.findByUsername(username);
        Map<String, String> response = new HashMap<>();
        response.put("avatar", user.map(User::getAvatar).orElse("")); // JSON {"avatar": "данные"}
        return ResponseEntity.ok(response);
    }




    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        Optional<User> existingUser = userService.findByUsername(user.getUsername());

        if (existingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        // Проверяем пароль
        if (userService.checkPassword(user.getPassword(), existingUser.get().getPasswordHash())) {
            return ResponseEntity.ok("Вход успешен");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Неверный пароль");
        }
    }


}
