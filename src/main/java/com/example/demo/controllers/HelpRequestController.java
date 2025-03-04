package com.example.demo.controllers;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.HelpRequestRepository;
import com.example.demo.services.HelpRequestService;
import com.example.demo.services.HelpResponseService;
import com.example.demo.services.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/help-requests")
@CrossOrigin(origins = "*") // Разрешает запросы с любого источника (можно настроить)
public class HelpRequestController {
    private HelpResponseService helpResponseService;
    private final HelpRequestService helpRequestService;
    private final UserRepository userRepository; // Добавили UserRepository
    private final HelpRequestRepository helpRequestRepository; // Добавили HelpRequestRepository
    private final HelpResponseRepository helpResponseRepository; // Добавили HelpRequestRepository
    private final UserService userService; // Добавляем зависимость от UserService

    private static final Logger logger = LoggerFactory.getLogger(HelpRequestController.class); // Добавляем логгер

    @Autowired
    public HelpRequestController(HelpRequestService helpRequestService,
                                 UserRepository userRepository,
                                 HelpRequestRepository helpRequestRepository, HelpResponseRepository helpResponseRepository,
                                 UserService userService) {
        this.helpRequestService = helpRequestService;
        this.userRepository = userRepository;
        this.helpRequestRepository = helpRequestRepository;
        this.helpResponseRepository = helpResponseRepository;
        this.userService = userService; // Присваиваем зависимость

    }
    @Transactional
    @PostMapping("/accept")
    public ResponseEntity<?> acceptHelpRequest(@RequestParam String requestUsername, @RequestParam String responderUsername) {
        try {
            logger.info("Попытка принять запрос: requestUsername={}, responderUsername={}", requestUsername, responderUsername);

            // Получаем пользователя, который отправил запрос
            User requestUser = userService.findByUsername(requestUsername)
                    .orElseThrow(() -> new RuntimeException("Пользователь с таким запросом не найден"));
            logger.info("Пользователь с запросом найден: {}", requestUser.getUsername());

            // Получаем пользователя, который отвечает на запрос
            User responder = userRepository.findByUsername(responderUsername)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            logger.info("Пользователь-ответчик найден: {}", responder.getUsername());

            // Проверяем, что responder не является создателем запроса
            if (requestUser.getUsername().equals(responderUsername)) {
                logger.warn("Попытка принять свой собственный запрос: {}", responderUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете принять свой собственный запрос");
            }

            // Находим запрос помощи
            HelpRequest helpRequest = helpRequestRepository.findAll().stream()
                    .filter(request -> request.getUser().getUsername().equals(requestUsername))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Запрос не найден"));
            logger.info("Запрос помощи найден: {}", helpRequest.getId());

            // Создаем новый ответ на запрос
            HelpResponse helpResponse = new HelpResponse();
            helpResponse.setHelpRequest(helpRequest);
            helpResponse.setResponder(responder);
            helpResponse.setCompleted(false); // Статус "не завершено"
            helpResponse.setCreatedAt(LocalDateTime.now());
            logger.info("Создан новый ответ на запрос: {}", helpResponse.getId());

            // Сохраняем ответ в базе данных
            logger.info("Ответ на запрос: {}", helpResponse);
            helpResponseRepository.save(helpResponse);
            logger.info("Ответ сохранен с ID: {}", helpResponse.getId());
            logger.info("Ответ на запрос сохранен в базе данных");

            return ResponseEntity.ok("Запрос принят");
        } catch (Exception e) {
            logger.error("Ошибка при принятии запроса: {}", e.getMessage(), e); // Логируем ошибку с деталями
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при принятии запроса: " + e.getMessage());
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> createHelpRequest(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String category = request.get("category");
            String price = request.get("price");
            String description = request.get("description");

            // Ищем пользователя по имени
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Создаем новый запрос
            HelpRequest helpRequest = new HelpRequest();
            helpRequest.setUser(user);  // Теперь user_id берется из объекта User
            helpRequest.setCategory(category);
            helpRequest.setPrice(Integer.valueOf(price));
            helpRequest.setDescription(description);
            helpRequest.setCreatedAt(LocalDateTime.now());

            // Сохраняем в БД
            helpRequestRepository.save(helpRequest);

            return ResponseEntity.ok("Запрос помощи успешно создан!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при создании запроса: " + e.getMessage());
        }
    }

    // Получение всех запросов, кроме запросов текущего пользователя
    @GetMapping("/all")
    public ResponseEntity<List<HelpRequest>> getAllHelpRequests(@RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Получаем все запросы, кроме тех, которые были отправлены текущим пользователем
        List<HelpRequest> helpRequests = helpRequestRepository.findAll().stream()
                .filter(request -> !request.getUser().getUsername().equals(username))
                .collect(Collectors.toList());

        return ResponseEntity.ok(helpRequests);
    }

}
