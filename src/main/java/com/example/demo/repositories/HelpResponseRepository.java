package com.example.demo.repositories;

import com.example.demo.models.HelpResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpResponseRepository extends JpaRepository<HelpResponse, Long> {
}
