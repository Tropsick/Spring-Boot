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
import java.util.HashMap;
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

            // Проверяем, хватает ли кармы
            int requiredKarma = 10; // Минимальная карма для создания запроса
            if (user.getKarma() < requiredKarma) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Недостаточно кармы для создания запроса. Необходимо минимум " + requiredKarma);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании запроса: " + e.getMessage());
        }
    }
    @GetMapping("/single")
    public ResponseEntity<?> getSingleHelpRequest(@RequestParam String username) {
        try {
            // Ищем пользователя по имени
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Ищем первый доступный запрос помощи, принадлежащий текущему пользователю
            Optional<HelpRequest> helpRequest = helpRequestRepository.findAll().stream()
                    .filter(request -> request.getUser().getId().equals(user.getId())) // Показываем запросы текущего пользователя по ID
                    .findFirst(); // Берем первый найденный запрос

            if (helpRequest.isEmpty()) {
                return ResponseEntity.ok().body(Map.of("message", "Нет доступных запросов для этого пользователя")); // Нет доступных запросов
            }

            HelpRequest request = helpRequest.get();

            // Создаем новый объект для ответа с только необходимыми полями
            Map<String, Object> response = new HashMap<>();
            response.put("category", request.getCategory());
            response.put("price", request.getPrice());
            response.put("description", request.getDescription());

            // Проверяем, есть ли отклики
            List<HelpResponse> responses = helpResponseRepository.findByHelpRequest(request);

            if (!responses.isEmpty()) {
                // Берем первого откликнувшегося пользователя по его ID
                Long responderId = responses.get(0).getResponder().getId();
                User responder = userRepository.findById(responderId).orElse(null); // Ищем пользователя по ID

                if (responder != null) {
                    String responderUsername = responder.getUsername(); // Получаем имя пользователя
                    response.put("responder", responderUsername);
                } else {
                    response.put("responder", "Неизвестный пользователь");
                }
            } else {
                response.put("responder", "Никто");
            }

            // Логирование для отладки
            System.out.println("Response: " + response); // Логируем ответ для проверки

            // Возвращаем ответ с нужными полями
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении запроса: " + e.getMessage());
        }
    }



    @PostMapping("/cancel")
    @Transactional
    public ResponseEntity<?> cancelRequest(@RequestParam String username) {
        try {
            // Ищем пользователя по username
            User requestUser = userRepository.findByUsername(username).orElse(null);
            if (requestUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Ищем первый открытый запрос помощи этого пользователя (не завершенный)
            HelpRequest helpRequest = helpRequestRepository.findOpenRequestByUser(requestUser).orElse(null);
            if (helpRequest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("У вас нет активных запросов");
            }

            // Удаляем запрос
            helpRequestRepository.delete(helpRequest);
            return ResponseEntity.ok("Запрос отменен успешно");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при отмене запроса: " + e.getMessage());
        }
    }

    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<?> confirmRequest(@RequestParam String username) {
        try {
            // Ищем пользователя по username
            User requestUser = userRepository.findByUsername(username).orElse(null);
            if (requestUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }



            // Получаем отклики на запрос
            List<HelpResponse> responses = helpResponseRepository.findByHelpRequest(helpRequest);
            if (responses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Нет откликов на этот запрос");
            }

            // Выбираем первого откликнувшегося пользователя
            HelpResponse helpResponse = responses.get(0);
            User responder = helpResponse.getResponder();

            // Обновляем статус отклика на завершенный
            helpResponse.setIsCompleted(true);
            helpResponseRepository.save(helpResponse);

            // Уменьшаем карму пользователя, который сделал запрос
            int price = helpRequest.getPrice();
            requestUser.setKarma(requestUser.getKarma() - price);

            // Увеличиваем карму откликнувшегося пользователя
            responder.setKarma(responder.getKarma() + price);

            // Сохраняем изменения в карме пользователей
            userRepository.save(requestUser);
            userRepository.save(responder);

            return ResponseEntity.ok("Запрос подтвержден успешно");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при подтверждении запроса: " + e.getMessage());
        }
    }



    @GetMapping("/count")
    public ResponseEntity<Long> getHelpRequestResponseCount(@RequestParam Long requestId) {
        long count = helpResponseRepository.countByHelpRequest(helpRequestRepository.findById(requestId).orElse(null));
        return ResponseEntity.ok(count);
    }

    @GetMapping("/all")
    public ResponseEntity<List<HelpRequest>> getAllHelpRequests(@RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Получаем все запросы и фильтруем их
        List<HelpRequest> helpRequests = helpRequestRepository.findAll().stream()
                .filter(request -> !request.getUser().getUsername().equals(username)) // Исключаем запросы текущего пользователя
                .filter(request -> helpResponseRepository.countByHelpRequest(request) == 0) // Оставляем только запросы без откликов
                .collect(Collectors.toList());

        return ResponseEntity.ok(helpRequests);
    }

}
