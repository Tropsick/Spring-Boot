package com.example.demo.services;

import com.example.demo.models.HelpRequest;
import com.example.demo.repositories.HelpRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;

    public HelpRequestService(HelpRequestRepository helpRequestRepository) {
        this.helpRequestRepository = helpRequestRepository;
    }

    public HelpRequest addHelpRequest(HelpRequest request) {
        request.setCreatedAt(LocalDateTime.now()); // Устанавливаем текущую дату
        return helpRequestRepository.save(request);
    }

    public List<HelpRequest> getAllHelpRequests() {
        return helpRequestRepository.findAll();
    }
}
