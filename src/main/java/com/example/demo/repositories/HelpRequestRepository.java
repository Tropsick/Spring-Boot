package com.example.demo.repositories;

import com.example.demo.models.HelpRequest;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {

    @Query("SELECT h FROM HelpRequest h WHERE h.user = :user AND NOT EXISTS (SELECT r FROM HelpResponse r WHERE r.helpRequest = h)")
    Optional<HelpRequest> findOpenRequestByUser(@Param("user") User user);
    @Query("""
    SELECT h FROM HelpRequest h 
    WHERE h.user = :user 
    AND EXISTS (
        SELECT r FROM HelpResponse r 
        WHERE r.helpRequest = h AND r.isCompleted = false
    )
""")
    Optional<HelpRequest> findOpenRequestByUserWithResp(@Param("user") User user);

}
