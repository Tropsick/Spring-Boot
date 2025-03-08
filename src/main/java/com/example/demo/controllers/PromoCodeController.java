package com.example.demo.controllers;

import com.example.demo.models.PromoCode;
import com.example.demo.models.User;
import com.example.demo.repositories.PromoCodeRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.KarmaService;
import com.example.demo.services.PromoCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/promo")
public class PromoCodeController {
    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;
    private final KarmaService karmaService;
    private final PromoCodeService promoCodeService;


    public PromoCodeController(PromoCodeRepository promoCodeRepository,PromoCodeService promoCodeService, UserRepository userRepository, KarmaService karmaService) {
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
        this.karmaService = karmaService;
        this.promoCodeService = promoCodeService;
    }






    // Получить информацию о промокоде
    @GetMapping("/{code}")
    public ResponseEntity<?> getPromoCode(@PathVariable String code) {
        Optional<PromoCode> promoCode = promoCodeService.getPromoCodeDetails(code);

        if (promoCode.isPresent()) {
            PromoCode promo = promoCode.get();
            return ResponseEntity.ok(promo); // Возвращаем компанию, описание и цену
        } else {
            return ResponseEntity.notFound().build(); // Промокод не найден
        }
    }
    @GetMapping("/price/{code}")
    public ResponseEntity<?> getPromoCodePrice(@PathVariable String code) {
        Optional<PromoCode> promoCode = promoCodeService.getPromoCodeByCode(code);

        if (promoCode.isPresent()) {
            return ResponseEntity.ok(promoCode.get().getPrice());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Промокод не найден");
        }
    }
    @PostMapping("/buy/{username}/{promoCode}")
    public ResponseEntity<String> buyPromoCode(@PathVariable String username, @PathVariable String promoCode) {
        Optional<PromoCode> promoOptional = promoCodeRepository.findByCode(promoCode);
        if (promoOptional.isEmpty()) {
            return ResponseEntity.notFound().build(); // Промокод не найден
        }

        PromoCode promo = promoOptional.get();
        if (!promo.isActive()) {
            return ResponseEntity.badRequest().body("Промокод уже неактивен"); // Промокод неактивен
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        Optional<User> shopOptional = userRepository.findByUsername("Shop");

        if (userOptional.isEmpty() || shopOptional.isEmpty()) {
            return ResponseEntity.notFound().build(); // Пользователь или магазин не найдены
        }

        User user = userOptional.get();
        User shop = shopOptional.get();

        if (user.getKarma() < promo.getPrice()) {
            return ResponseEntity.badRequest().body("Недостаточно кармы для покупки"); // Недостаточно кармы
        }

        // Переводим карму через сервис кармы
        ResponseEntity<?> transferResponse = karmaService.transferKarma(user.getUsername(), shop.getUsername(), promo.getPrice());
        if (!transferResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при переводе кармы: " + transferResponse.getBody());
        }

        // Делаем промокод неактивным после покупки
        promo.setActive(false);
        promoCodeRepository.save(promo);

        return ResponseEntity.ok("Промокод куплен успешно! Ваш промокод: " + promo.getCode()); // Возвращаем успешный ответ с кодом
    }


    // Получить список всех активных промокодов (без кода промокода)
    @GetMapping("/all")
    public ResponseEntity<?> getAllActivePromoCodes() {
        List<PromoCode> activePromoCodes = promoCodeService.getActivePromoCodes();

        if (activePromoCodes.isEmpty()) {
            return ResponseEntity.noContent().build(); // Если нет активных промокодов
        } else {
            // Убираем код промокода из ответа
            List<PromoCode> filteredPromoCodes = activePromoCodes.stream()
                    .map(promo -> new PromoCode(
                            promo.getId(),
                            promo.getCode(), // Код промокода не передаем
                            promo.getPrice(),
                            promo.getDescription(),
                            promo.getCompanyName(),
                            promo.isActive()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(filteredPromoCodes);
        }
    }
}
