package com.example.demo.controllers;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.services.HelpResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    @GetMapping("/cancel")
    public ResponseEntity<?> cancelHelpResponse(@RequestParam String username) {
        try {
            // Ищем пользователя по имени
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Находим отклик пользователя, который не завершен
            Optional<HelpResponse> helpResponse = helpResponseRepository.findAll().stream()
                    .filter(response -> response.getResponder().equals(user)) // Отклики пользователя
                    .filter(response -> !response.isCompleted()) // Только незавершенные отклики
                    .findFirst();

            // Если отклик найден, удаляем его
            if (helpResponse.isPresent()) {
                helpResponseRepository.delete(helpResponse.get());
                return ResponseEntity.ok().body("Отклик был успешно удален.");
            } else {
                // Если отклик не найден или завершен
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Нет откликов или они завершены");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при удалении отклика: " + e.getMessage());
        }
    }

    @GetMapping("/single")
    public ResponseEntity<?> getSingleHelpResponse(@RequestParam String username) {
        try {
            // Ищем пользователя по имени
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Находим отклик пользователя, который не завершен
            Optional<HelpResponse> helpResponse = helpResponseRepository.findAll().stream()
                    .filter(response -> response.getResponder().equals(user)) // Отклики пользователя
                    .filter(response -> !response.isCompleted()) // Только незавершенные отклики
                    .findFirst();

            // Если отклик найден, возвращаем запрос о помощи, с которым он связан
            if (helpResponse.isPresent()) {
                HelpRequest helpRequest = helpResponse.get().getHelpRequest(); // Получаем запрос, с которым связан отклик
                return ResponseEntity.ok().body(Map.of(
                        "user", helpRequest.getUser().getUsername(), // Имя пользователя, который создал запрос
                        "category", helpRequest.getCategory(), // Категория запроса
                        "price", helpRequest.getPrice(), // Цена запроса
                        "description", helpRequest.getDescription() // Описание запроса
                ));
            } else {
                // Если отклик не найден или завершен
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Нет откликов или они завершены");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении отклика: " + e.getMessage());
        }
    }

}
