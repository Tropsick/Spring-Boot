package com.example.demo.services;

import com.example.demo.models.PromoCode;
import com.example.demo.models.User;
import com.example.demo.repositories.PromoCodeRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;
    private final KarmaService karmaService; // Добавляем сервис кармы

    public PromoCodeService(PromoCodeRepository promoCodeRepository, UserRepository userRepository, KarmaService karmaService) {
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
        this.karmaService = karmaService;
    }

    public List<PromoCode> getAllPromoCodes() {
        return promoCodeRepository.findAll();
    }

    public Optional<PromoCode> getPromoCodeByCode(String code) {
        return promoCodeRepository.findByCode(code);
    }

    public void createPromoCode(PromoCode promoCode) {
        promoCodeRepository.save(promoCode);
    }

    public String buyPromoCode(String username, String promoCode) {
        Optional<PromoCode> promoOptional = promoCodeRepository.findByCode(promoCode);
        if (promoOptional.isEmpty()) {
            return "Промокод не найден";
        }

        PromoCode promo = promoOptional.get();
        if (!promo.isActive()) {
            return "Промокод уже неактивен";
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        Optional<User> shopOptional = userRepository.findByUsername("Shop");

        if (userOptional.isEmpty() || shopOptional.isEmpty()) {
            return "Пользователь или магазин не найдены";
        }

        User user = userOptional.get();
        User shop = shopOptional.get();

        if (user.getKarma() < promo.getPrice()) {
            return "Недостаточно кармы для покупки";
        }

        // Perform karma transfer
        ResponseEntity<?> transferResponse = karmaService.transferKarma(user.getUsername(), shop.getUsername(), promo.getPrice());
        if (!transferResponse.getStatusCode().is2xxSuccessful()) {
            return "Ошибка при переводе кармы: " + transferResponse.getBody();
        }

        // Mark promo code as inactive
        promo.setActive(false);
        promoCodeRepository.save(promo);

        return promo.getCode(); // Return the code of the purchased promo code
    }

    public Optional<PromoCode> getPromoCodeDetails(String code) {
        return promoCodeRepository.findByCode(code);
    }

    public List<PromoCode> getActivePromoCodes() {
        return promoCodeRepository.findByIsActiveTrue();
    }
}
