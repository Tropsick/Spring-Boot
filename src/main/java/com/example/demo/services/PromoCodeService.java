package com.example.demo.services;

import com.example.demo.models.PromoCode;
import com.example.demo.models.User;
import com.example.demo.repositories.PromoCodeRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository, UserRepository userRepository) {
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
    }

    // Метод для получения промокода по коду
    public Optional<PromoCode> getPromoCodeByCode(String code) {
        return promoCodeRepository.findByCode(code);
    }

    // Метод для покупки промокода
    public String buyPromoCode(String username, String promoCode) {
        Optional<User> user = userRepository.findByUsername(username);
        Optional<PromoCode> code = promoCodeRepository.findByCode(promoCode);

        if (user.isPresent() && code.isPresent()) {
            PromoCode promo = code.get();
            User currentUser = user.get();

            if (currentUser.getKarma() >= promo.getCost()) {
                // Если у пользователя достаточно кармы для покупки
                currentUser.setKarma(currentUser.getKarma() - promo.getCost());

                // Добавляем карму пользователю Shop
                Optional<User> shopUser = userRepository.findByUsername("Shop");
                shopUser.ifPresent(shop -> shop.setKarma(shop.getKarma() + promo.getCost()));

                // Сохраняем обновления
                userRepository.save(currentUser);
                userRepository.save(shopUser.get());

                return promo.getCode(); // Возвращаем промокод пользователю
            } else {
                return "Недостаточно кармы для покупки"; // Ошибка, если кармы не хватает
            }
        } else {
            return "Промокод не найден"; // Ошибка, если промокод не найден
        }
    }

    // Метод для отображения всех доступных промокодов
    public Optional<PromoCode> getPromoCodeDetails(String promoCode) {
        return promoCodeRepository.findByCode(promoCode);
    }
}
