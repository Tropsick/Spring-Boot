package com.example.demo.controllers;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.HelpRequestRepository;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/help-responses")
public class HelpResponseController {

    @Autowired
    private HelpResponseRepository helpResponseRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private UserRepository userRepository;

    // Принять запрос на помощь
    @PostMapping("/accept")
    public HelpResponse acceptHelpRequest(@RequestParam String requestUsername, @RequestParam String responderUsername) {
        // Ищем пользователя по имени
        Optional<User> requestUser = userRepository.findByUsername(requestUsername);
        Optional<User> responderUser = userRepository.findByUsername(responderUsername);

        if (!requestUser.isPresent() || !responderUser.isPresent()) {
            throw new RuntimeException("Пользователь не найден");
        }

        // Ищем запрос помощи, который еще не принят
        Optional<HelpRequest> helpRequest = helpRequestRepository.findByUserAndIsCompletedFalse(requestUser.get());

        if (!helpRequest.isPresent()) {
            throw new RuntimeException("Нет доступных запросов помощи");
        }

        HelpResponse helpResponse = new HelpResponse();
        helpResponse.setHelpRequest(helpRequest.get());
        helpResponse.setResponder(responderUser.get());
        helpResponse.setCompleted(false);
        helpResponse.setCreatedAt(LocalDateTime.now());

        // Сохраняем новый ответ
        return helpResponseRepository.save(helpResponse);
    }

    // Получить все ответы на запросы
    @GetMapping("/all")
    public List<HelpResponse> getAllHelpResponses() {
        return helpResponseRepository.findAll();
    }

    // Получить ответы на конкретный запрос
    @GetMapping("/request/{requestId}")
    public List<HelpResponse> getHelpResponsesForRequest(@PathVariable Long requestId) {
        return helpResponseRepository.findByHelpRequestId(requestId);
    }

    // Пример завершения помощи
    @PutMapping("/complete/{responseId}")
    public HelpResponse completeHelpResponse(@PathVariable Long responseId) {
        Optional<HelpResponse> response = helpResponseRepository.findById(responseId);
        if (response.isPresent()) {
            HelpResponse helpResponse = response.get();
            helpResponse.setCompleted(true);
            return helpResponseRepository.save(helpResponse);
        } else {
            throw new RuntimeException("Ответ не найден");
        }
    }
}
