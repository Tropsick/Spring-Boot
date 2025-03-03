package com.example.demo.repositories;

import com.example.demo.models.HelpResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelpResponseRepository extends JpaRepository<HelpResponse, Long> {
    // Методы для работы с таблицей help_responses, если нужно
}