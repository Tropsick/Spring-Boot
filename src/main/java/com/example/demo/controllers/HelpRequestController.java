package com.example.demo.controllers;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.KarmaHistory;
import com.example.demo.models.User;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.repositories.KarmaHistoryRepository;
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
    private final KarmaHistoryRepository karmaHistoryRepository;


    private static final Logger logger = LoggerFactory.getLogger(HelpRequestController.class);

    @Autowired
    public HelpRequestController(HelpRequestService helpRequestService,
                                 UserRepository userRepository,
                                 HelpRequestRepository helpRequestRepository,
                                 HelpResponseRepository helpResponseRepository,
                                 UserService userService,
                                 KarmaHistoryRepository karmaHistoryRepository) {
        this.helpRequestService = helpRequestService;
        this.userRepository = userRepository;
        this.helpRequestRepository = helpRequestRepository;
        this.helpResponseRepository = helpResponseRepository;
        this.userService = userService;
        this.karmaHistoryRepository = karmaHistoryRepository;

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

            // Получаем все запросы помощи для текущего пользователя
            List<HelpRequest> allRequests = helpRequestRepository.findAll().stream()
                    .filter(request -> request.getUser().getId().equals(user.getId())) // Запросы текущего пользователя по ID
                    .collect(Collectors.toList()); // Собираем в список

            // Ищем первый не завершенный запрос
            for (HelpRequest request : allRequests) {
                // Проверяем отклики на запрос
                List<HelpResponse> responses = helpResponseRepository.findByHelpRequest(request);

                // Если отклики есть, и хотя бы один отклик завершён, пропускаем этот запрос
                boolean isRequestCompleted = responses.stream().anyMatch(HelpResponse::isCompleted);
                if (isRequestCompleted) {
                    continue; // Если запрос выполнен, пропускаем
                }

                // Если запрос ещё не завершен, возвращаем его
                Map<String, Object> response = new HashMap<>();
                response.put("category", request.getCategory());
                response.put("price", request.getPrice());
                response.put("description", request.getDescription());

                if (!responses.isEmpty()) {
                    // Берем первого откликнувшегося пользователя
                    HelpResponse firstResponse = responses.get(0);
                    User responder = firstResponse.getResponder();

                    if (responder != null) {
                        response.put("responder", responder.getUsername());
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
            }

            // Если нет доступных запросов, возвращаем сообщение
            return ResponseEntity.ok().body(Map.of("message", "Нет доступных запросов для этого пользователя"));

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
            HelpRequest helpRequest = helpRequestRepository.findOpenRequestByUserWithResp(requestUser).orElse(null);
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
            // Ищем пользователя, который создавал запрос
            User requestUser = userRepository.findByUsername(username).orElse(null);
            if (requestUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Ищем первый активный запрос помощи с откликами
            HelpRequest helpRequest = helpRequestRepository.findOpenRequestByUserWithResp(requestUser).orElse(null);
            if (helpRequest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("У вас нет активных запросов");
            }

            // Получаем отклики
            List<HelpResponse> responses = helpResponseRepository.findByHelpRequest(helpRequest);
            if (responses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Нет откликов на этот запрос");
            }

            // Берем первого откликнувшегося
            HelpResponse helpResponse = responses.get(0);
            User responder = helpResponse.getResponder();

            // Если отклик уже завершен, ничего не делаем
            if (helpResponse.isCompleted()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Этот запрос уже подтвержден");
            }

            // Обновляем статус отклика
            helpResponse.setIsCompleted(true);
            helpResponseRepository.save(helpResponse);

            // Передача кармы
            int price = helpRequest.getPrice();
            if (requestUser.getKarma() < price) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недостаточно кармы для передачи");
            }

            requestUser.setKarma(requestUser.getKarma() - price);

            // Рассчитываем, сколько кармы можно прибавить, чтобы не превысить 100
            int newKarma = responder.getKarma() + price;
            int actualAddedKarma = Math.min(price, 100 - responder.getKarma()); // Сколько реально прибавится
            responder.setKarma(Math.min(newKarma, 100)); // Устанавливаем ограничение

            userRepository.save(requestUser);
            userRepository.save(responder);

            // Записываем в историю передачу кармы
            KarmaHistory karmaHistory = new KarmaHistory();
            karmaHistory.setSender(requestUser);
            karmaHistory.setReceiver(responder);
            karmaHistory.setAmount(actualAddedKarma); // Фиксируем реальное количество переданной кармы
            karmaHistoryRepository.save(karmaHistory);

            return ResponseEntity.ok("Запрос подтвержден успешно, передано " + actualAddedKarma + " кармы");
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
