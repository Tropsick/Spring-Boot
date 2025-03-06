package com.example.demo.controllers;

import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.services.HelpResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/help-responses")
public class HelpResponseController {

    @Autowired
    private HelpResponseService helpResponseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HelpResponseRepository helpResponseRepository;

    // Принять запрос на помощь
    @PostMapping("/accept")
    public HelpResponse acceptHelpRequest(@RequestParam String requestUsername, @RequestParam String responderUsername) {
        return helpResponseService.createHelpResponse(requestUsername, responderUsername);
    }

    // Получить один отклик пользователя (если он есть и он не завершен)
    @GetMapping("/single")
    public ResponseEntity<?> getSingleHelpResponse(@RequestParam String username) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            Optional<HelpResponse> helpResponse = helpResponseRepository.findAll().stream()
                    .filter(response -> response.getResponder().equals(user)) // Отклики пользователя
                    .filter(response -> !response.isCompleted()) // Только незавершенные отклики
                    .findFirst();

            return helpResponse.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.ok().body(null)); // Возвращаем пустое тело вместо ошибки
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении отклика: " + e.getMessage());
        }
    }
}
