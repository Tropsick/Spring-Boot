package com.example.demo.controllers;


import com.example.demo.models.KarmaHistory;
import com.example.demo.models.User;
import com.example.demo.repositories.KarmaHistoryRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.KarmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/karma")
public class KarmaController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KarmaHistoryRepository karmaHistoryRepository;

    @Autowired
    private KarmaService karmaService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transferKarma(
            @RequestParam String sender,
            @RequestParam String receiver,
            @RequestParam int amount) {
        return karmaService.transferKarma(sender, receiver, amount);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getKarmaHistory(@RequestParam String username) {
        try {
            // Ищем пользователя по имени
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Получаем всю историю кармы, где этот пользователь выступает либо отправителем, либо получателем
            List<KarmaHistory> karmaHistoryList = karmaHistoryRepository.findBySenderOrReceiver(user, user);

            if (karmaHistoryList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("История кармы пуста");
            }

            // Формируем ответ, чтобы показать, кто и сколько кармы передал
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (KarmaHistory karmaHistory : karmaHistoryList) {
                Map<String, Object> responseItem = new HashMap<>();

                // Определяем, кто передал и кто получил карму
                if (karmaHistory.getSender().getId().equals(user.getId())) {
                    responseItem.put("sender", karmaHistory.getSender().getUsername());
                    responseItem.put("receiver", karmaHistory.getReceiver().getUsername());
                    responseItem.put("amount", -karmaHistory.getAmount()); // Отправлено отрицательное количество кармы
                } else {
                    responseItem.put("sender", karmaHistory.getSender().getUsername());
                    responseItem.put("receiver", karmaHistory.getReceiver().getUsername());
                    responseItem.put("amount", karmaHistory.getAmount()); // Получено положительное количество кармы
                }
                responseList.add(responseItem);
            }

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении истории кармы: " + e.getMessage());
        }
    }

}
