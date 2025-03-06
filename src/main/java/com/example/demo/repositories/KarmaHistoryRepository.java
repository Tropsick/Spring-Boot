package com.example.demo.repositories;


import com.example.demo.models.KarmaHistory;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KarmaHistoryRepository extends JpaRepository<KarmaHistory, Long> {
    List<KarmaHistory> findBySender(User sender);
    List<KarmaHistory> findByReceiver(User receiver);
}
