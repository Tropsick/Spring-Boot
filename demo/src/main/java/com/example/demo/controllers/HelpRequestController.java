package com.example.demo.controllers;

import com.example.demo.models.HelpRequest;
import com.example.demo.repositories.HelpRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/help-requests")
public class HelpRequestController {

    private final HelpRequestRepository helpRequestRepository;

    @Autowired
    public HelpRequestController(HelpRequestRepository helpRequestRepository) {
        this.helpRequestRepository = helpRequestRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createHelpRequest(@RequestBody HelpRequest request) {
        try {
            HelpRequest savedRequest = helpRequestRepository.save(request);
            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании запроса: " + e.getMessage());
        }
    }
}
