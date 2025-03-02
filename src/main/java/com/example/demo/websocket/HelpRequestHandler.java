package com.example.demo.websocket;

import com.example.demo.models.HelpRequest;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class HelpRequestHandler {

    @MessageMapping("/help") // Клиенты отправляют запросы на /app/help
    @SendTo("/topic/requests") // Все подписанные клиенты получат обновление на /topic/requests
    public HelpRequest handleHelpRequest(HelpRequest request) {
        System.out.println("Новый запрос о помощи: " + request);
        return request; // Отправляем запрос обратно всем подписчикам
    }
}
