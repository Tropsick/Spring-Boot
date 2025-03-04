package com.example.demo.services;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.HelpRequestRepository;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HelpResponseService {
    private static final Logger logger = LoggerFactory.getLogger(HelpResponseService.class);

    @Autowired
    private HelpResponseRepository helpResponseRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private UserRepository userRepository;

    public HelpResponse createHelpResponse(String requestUsername, String responderUsername) {
        // Ищем пользователей по именам
        User requestUser = userRepository.findByUsername(requestUsername)
                .orElseThrow(() -> new RuntimeException("Запрашивающий пользователь не найден"));

        User responderUser = userRepository.findByUsername(responderUsername)
                .orElseThrow(() -> new RuntimeException("Респондер не найден"));
        // Логируем список всех запросов на помощь перед созданием отклика
        List<HelpRequest> allHelpRequests = helpRequestRepository.findAll();
        logAllHelpResponses();
        logger.info("Список всех запросов помощи:");
        for (HelpRequest hr : allHelpRequests) {
            logger.info("ID запроса: {}, Пользователь: {}, Описание: {}", hr.getId(), hr.getUser().getUsername(), hr.getDescription());
        }
        // Ищем активный запрос на помощь, у которого еще нет ответа
        HelpRequest helpRequest = helpRequestRepository.findOpenRequestByUser(requestUser)
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
    public void logAllHelpResponses() {
        logger.info("Список всех ответов помощи:");
        // Получаем все ответы из репозитория
        List<HelpResponse> allHelpResponses = helpResponseRepository.findAll();

        for (HelpResponse hr : allHelpResponses) {
            // Логируем информацию о каждом ответе
            logger.info("ID ответа: {}, ID запроса: {}, Ответчик: {}, Статус выполнения: {}, Дата создания: {}",
                    hr.getId(),
                    hr.getHelpRequest().getId(),
                    hr.getResponder().getUsername(),
                    hr.isCompleted() ? "Завершено" : "В процессе",
                    hr.getCreatedAt());
        }
    }
    public List<HelpResponse> getAllHelpResponses() {
        return helpResponseRepository.findAll();
    }

    public HelpResponse completeHelpResponse(Long responseId) {
        HelpResponse helpResponse = helpResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Ответ не найден"));

        helpResponse.setCompleted(true);

        // Логируем информацию о завершении отклика
        logger.info("Ответ с ID {} был завершен. Запрос: {}", helpResponse.getId(), helpResponse.getHelpRequest().getId());

        return helpResponseRepository.save(helpResponse);
    }
}

