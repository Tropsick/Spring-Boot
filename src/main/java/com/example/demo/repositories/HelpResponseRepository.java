package com.example.demo.repositories;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.HelpResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpResponseRepository extends JpaRepository<HelpResponse, Long> {
    List<HelpResponse> findAllByHelpRequest(HelpRequest helpRequest);

}

