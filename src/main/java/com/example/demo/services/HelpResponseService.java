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

        // Проверяем, есть ли у респондера уже незавершённый отклик
        boolean hasActiveResponse = helpResponseRepository.existsByResponderAndIsCompletedFalse(responderUser);
        if (hasActiveResponse) {
            throw new RuntimeException("Вы уже выполняете другой запрос, завершите его перед тем, как взять новый.");
        }

        // Ищем открытые запросы на помощь от requestUser, к которым еще нет отклика
        HelpRequest helpRequest = helpRequestRepository.findOpenRequestByUser(requestUser)
                .orElseThrow(() -> new RuntimeException("Нет доступных запросов помощи"));

        // Проверка, не оставил ли responder уже отклик на этот запрос
        List<HelpResponse> existingResponses = helpResponseRepository.findByHelpRequestAndResponder(helpRequest, responderUser);
        if (!existingResponses.isEmpty()) {
            throw new RuntimeException("Респондер уже оставил отклик на этот запрос");
        }

        // Создаем новый отклик
        HelpResponse helpResponse = new HelpResponse();
        helpResponse.setHelpRequest(helpRequest);
        helpResponse.setResponder(responderUser);
        helpResponse.setCompleted(false);
        helpResponse.setCreatedAt(LocalDateTime.now());

        // Логируем создание отклика
        logger.info("Респондер {} создал отклик на запрос помощи ID {}", responderUsername, helpRequest.getId());

        // Сохраняем отклик в БД
        return helpResponseRepository.save(helpResponse);
    }



    public void logAllHelpResponses() {
        logger.info("Список всех откликов на запросы помощи:");
        List<HelpResponse> allHelpResponses = helpResponseRepository.findAll();

        for (HelpResponse hr : allHelpResponses) {
            logger.info("ID отклика: {}, ID запроса: {}, Ответчик: {}, Статус: {}, Дата создания: {}",
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

        // Обновляем статус на завершено
        helpResponse.setCompleted(true);

        // Логируем завершение отклика
        logger.info("Отклик с ID {} был завершен для запроса с ID {}", helpResponse.getId(), helpResponse.getHelpRequest().getId());

        return helpResponseRepository.save(helpResponse);
    }
}
