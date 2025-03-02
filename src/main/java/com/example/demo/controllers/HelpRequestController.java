package com.example.demo.controllers;

import com.example.demo.models.HelpRequest;
import com.example.demo.services.HelpRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/help-requests")
@CrossOrigin(origins = "*") // Разрешает запросы с любого источника (можно настроить)
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    public HelpRequestController(HelpRequestService helpRequestService) {
        this.helpRequestService = helpRequestService;
    }

    // Создание запроса о помощи
    @PostMapping("/create")
    public ResponseEntity<?> createHelpRequest(@RequestBody HelpRequest request) {
        try {
            HelpRequest savedRequest = helpRequestService.addHelpRequest(request);
            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании запроса: " + e.getMessage());
        }
    }

    // Получение всех запросов (для списка в приложении)
    @GetMapping("/all")
    public ResponseEntity<List<HelpRequest>> getAllHelpRequests() {
        List<HelpRequest> requests = helpRequestService.getAllHelpRequests();
        return ResponseEntity.ok(requests);
    }
}
