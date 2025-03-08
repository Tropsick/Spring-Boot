package com.example.demo.controllers;

import com.example.demo.models.PromoCode;
import com.example.demo.services.PromoCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/promo")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
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

    // Купить промокод
    @PostMapping("/buy/{username}/{promoCode}")
    public ResponseEntity<String> buyPromoCode(@PathVariable String username, @PathVariable String promoCode) {
        String result = promoCodeService.buyPromoCode(username, promoCode);

        switch (result) {
            case "Недостаточно кармы для покупки":
                return ResponseEntity.badRequest().body(result);
            case "Промокод не найден":
                return ResponseEntity.notFound().build();
            case "Промокод уже неактивен":
                return ResponseEntity.badRequest().body(result);
            default:
                return ResponseEntity.ok("Промокод куплен успешно! Ваш промокод: " + result);
        }
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
                            null, // Код промокода не передаем
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
