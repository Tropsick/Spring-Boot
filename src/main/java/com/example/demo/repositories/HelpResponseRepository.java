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

    long countByHelpRequest(HelpRequest helpRequest);
    boolean existsByResponderAndIsCompletedFalse(User responder);

}
