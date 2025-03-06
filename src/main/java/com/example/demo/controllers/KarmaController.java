package com.example.demo.controllers;


import com.example.demo.models.KarmaHistory;
import com.example.demo.services.KarmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/karma")
public class KarmaController {
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
        List<KarmaHistory> history = karmaService.getHistoryByUser(username);
        return ResponseEntity.ok(history);
    }
}
