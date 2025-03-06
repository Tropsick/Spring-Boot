package com.example.demo.repositories;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelpResponseRepository extends JpaRepository<HelpResponse, Long> {

    // Метод для поиска отклика по запросу и респондеру
    List<HelpResponse> findByHelpRequestAndResponder(HelpRequest request, User user);

    // Метод для поиска всех откликов по запросу
    List<HelpResponse> findByHelpRequest(HelpRequest request);

    long countByHelpRequest(HelpRequest helpRequest);

    boolean existsByResponderAndIsCompletedFalse(User responder);
}
