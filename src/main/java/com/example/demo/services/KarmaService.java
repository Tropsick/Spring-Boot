package com.example.demo.services;


import com.example.demo.models.KarmaHistory;
import com.example.demo.models.User;
import com.example.demo.repositories.KarmaHistoryRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class KarmaService {
    @Autowired
    private KarmaHistoryRepository karmaHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> transferKarma(String senderUsername, String receiverUsername, int amount) {
        User sender = userRepository.findByUsername(senderUsername).orElse(null);
        User receiver = userRepository.findByUsername(receiverUsername).orElse(null);

        if (sender == null || receiver == null) {
            return ResponseEntity.badRequest().body("Один из пользователей не найден");
        }

        if (sender.getKarma() < amount) {
            return ResponseEntity.badRequest().body("Недостаточно кармы для передачи");
        }

        // Ограничиваем amount, чтобы у получателя не получилось больше 100 кармы
        int availableRoom = 100 - receiver.getKarma();
        if (availableRoom < amount) {
            amount = availableRoom;
        }

        // Обновляем карму пользователей
        sender.setKarma(sender.getKarma() - amount);
        receiver.setKarma(receiver.getKarma() + amount);
        userRepository.save(sender);
        userRepository.save(receiver);

        // Записываем операцию в историю
        KarmaHistory history = new KarmaHistory();
        history.setSender(sender);
        history.setReceiver(receiver);
        history.setAmount(amount);
        karmaHistoryRepository.save(history);

        return ResponseEntity.ok("Карма успешно передана!");
    }


    public List<KarmaHistory> getHistoryByUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Collections.emptyList();

        // Получаем историю по отправителю и получателю
        List<KarmaHistory> sent = karmaHistoryRepository.findBySender(user);
        List<KarmaHistory> received = karmaHistoryRepository.findByReceiver(user);

        // Объединяем и сортируем по дате (самые новые сверху)
        List<KarmaHistory> fullHistory = new ArrayList<>();
        fullHistory.addAll(sent);
        fullHistory.addAll(received);
        fullHistory.sort(Comparator.comparing(KarmaHistory::getCreatedAt).reversed());

        return fullHistory;
    }
}
