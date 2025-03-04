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
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/help-requests")
@CrossOrigin(origins = "*")
public class HelpRequestController {
    private final HelpRequestService helpRequestService;
    private final UserRepository userRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final HelpResponseRepository helpResponseRepository;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(HelpRequestController.class);

    @Autowired
    public HelpRequestController(HelpRequestService helpRequestService,
                                 UserRepository userRepository,
                                 HelpRequestRepository helpRequestRepository,
                                 HelpResponseRepository helpResponseRepository,
                                 UserService userService) {
        this.helpRequestService = helpRequestService;
        this.userRepository = userRepository;
        this.helpRequestRepository = helpRequestRepository;
        this.helpResponseRepository = helpResponseRepository;
        this.userService = userService;
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

            // Проверяем, есть ли уже активный запрос у пользователя
            HelpRequest existingRequest = helpRequestRepository.findOpenRequestByUser(user).orElse(null);
            if (existingRequest != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("У вас уже есть активный запрос");
            }

            // Создаем новый запрос
            HelpRequest helpRequest = new HelpRequest();
            helpRequest.setUser(user);
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

    @GetMapping("/all")
    public ResponseEntity<List<HelpRequest>> getAllHelpRequests(@RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Получаем все запросы, кроме тех, которые были отправлены текущим пользователем
        List<HelpRequest> helpRequests = helpRequestRepository.findAll().stream()
                .filter(request -> !request.getUser().getUsername().equals(username)) // Исключаем запросы текущего пользователя
                .filter(request -> {
                    // Проверяем, что нет завершенных откликов на запрос
                    Optional<HelpResponse> response = helpResponseRepository.findByHelpRequestAndResponder(request, user);
                    // Если отклик существует, проверяем его статус, если отклик нет - пропускаем
                    return response.map(r -> !r.isCompleted()).orElse(true);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(helpRequests);
    }



}
