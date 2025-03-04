package com.example.demo.services;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.HelpRequestRepository;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HelpResponseService {

    @Autowired
    private HelpResponseRepository helpResponseRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private UserRepository userRepository;

    public HelpResponse createHelpResponse(String requestUsername, String responderUsername) {
        // Ищем пользователей по именам
        User requestUser = userRepository.findByUsername(requestUsername)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        User responderUser = userRepository.findByUsername(responderUsername)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Ищем запрос помощи, который еще не принят
        HelpRequest helpRequest = helpRequestRepository.findByUserAndIsCompletedFalse(requestUser)
                .orElseThrow(() -> new RuntimeException("Нет доступных запросов помощи"));

        // Создаем новый отклик
        HelpResponse helpResponse = new HelpResponse();
        helpResponse.setHelpRequest(helpRequest);
        helpResponse.setResponder(responderUser);
        helpResponse.setCompleted(false);
        helpResponse.setCreatedAt(LocalDateTime.now());

        // Сохраняем новый отклик
        return helpResponseRepository.save(helpResponse);
    }

    public List<HelpResponse> getAllHelpResponses() {
        return helpResponseRepository.findAll();
    }

    public HelpResponse completeHelpResponse(Long responseId) {
        HelpResponse helpResponse = helpResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Ответ не найден"));

        helpResponse.setCompleted(true);
        return helpResponseRepository.save(helpResponse);
    }
}
