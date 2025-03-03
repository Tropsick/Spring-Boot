package com.example.demo.services;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.HelpRequestRepository;
import com.example.demo.repositories.HelpResponseRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelpResponseService {

    @Autowired
    private HelpResponseRepository helpResponseRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private UserRepository userRepository;

    public void createHelpResponse(Long requestId, Long responderId) {
        HelpRequest helpRequest = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Запрос не найден"));

        User responder = userRepository.findById(responderId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        HelpResponse helpResponse = new HelpResponse();
        helpResponse.setHelpRequest(helpRequest);
        helpResponse.setResponder(responder);
        helpResponse.setCompleted(false);

        helpResponseRepository.save(helpResponse);
    }
}
