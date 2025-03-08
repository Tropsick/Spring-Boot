package com.example.demo.services;

import com.example.demo.models.PromoCode;
import com.example.demo.models.User;
import com.example.demo.repositories.PromoCodeRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository; // Добавляем UserRepository

    public PromoCodeService(PromoCodeRepository promoCodeRepository, UserRepository userRepository) {
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
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

        if (userOptional.isEmpty()) {
            return "Пользователь не найден";
        }

        User user = userOptional.get();

        if (user.getKarma() < promo.getPrice()) {
            return "Недостаточно кармы для покупки";
        }

        // Вычитаем стоимость из кармы пользователя
        user.setKarma(user.getKarma() - promo.getPrice());
        userRepository.save(user);

        // Делаем промокод неактивным
        promo.setActive(false);
        promoCodeRepository.save(promo);

        return promo.getCode(); // Возвращаем код купленного промокода

    }
    // Получить детали промокода
    public Optional<PromoCode> getPromoCodeDetails(String code) {
        return promoCodeRepository.findByCode(code);
    }

    // Получить список всех активных промокодов
    public List<PromoCode> getActivePromoCodes() {
        return promoCodeRepository.findByIsActiveTrue();
    }
}
