package com.example.demo.controllers;

import com.example.demo.models.HelpResponse;
import com.example.demo.services.HelpResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/help-responses")
public class HelpResponseController {

    @Autowired
    private HelpResponseService helpResponseService;

    // Принять запрос на помощь
    @PostMapping("/accept")
    public HelpResponse acceptHelpRequest(@RequestParam String requestUsername, @RequestParam String responderUsername) {
        return helpResponseService.createHelpResponse(requestUsername, responderUsername);
    }

    // Получить все ответы на запросы
    @GetMapping("/all")
    public List<HelpResponse> getAllHelpResponses() {
        return helpResponseService.getAllHelpResponses();
    }


}
