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

    /**
     * Ищем запрос пользователя, у которого нет отклика.
     *
     * @param user пользователь, чьи запросы ищем
     * @return первый открытый запрос без отклика
     */
    @Query("SELECT h FROM HelpRequest h WHERE h.user = :user AND NOT EXISTS (SELECT r FROM HelpResponse r WHERE r.helpRequest = h)")
    Optional<HelpRequest> findOpenRequestByUser(@Param("user") User user);

    /**
     * Получаем все открытые запросы (которые не завершены и не отправлены самим пользователем)
     *
     * @param user пользователь, чьи запросы не должны попадать в результаты
     * @return список всех запросов, кроме запросов текущего пользователя
     */
    @Query("SELECT h FROM HelpRequest h WHERE h.user != :user AND NOT EXISTS (SELECT r FROM HelpResponse r WHERE r.helpRequest = h AND r.completed = true)")
    List<HelpRequest> findAllOpenRequestsExceptUser(@Param("user") User user);
}
