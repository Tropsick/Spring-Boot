package com.example.demo.controllers;

import com.example.demo.models.PromoCode;
import com.example.demo.services.PromoCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

        if (result.equals("Недостаточно кармы для покупки")) {
            return ResponseEntity.badRequest().body(result); // Ошибка недостаточно кармы
        } else if (result.equals("Промокод не найден")) {
            return ResponseEntity.notFound().build(); // Промокод не найден
        } else {
            return ResponseEntity.ok("Промокод куплен успешно! Ваш промокод: " + result); // Успешная покупка
        }
    }
}
