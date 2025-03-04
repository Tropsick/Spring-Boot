package com.example.demo.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class HelpResponse {
    @Id
    private Long id;  // Не указываем @GeneratedValue, так как инкремент будет в БД

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private HelpRequest helpRequest;

    @ManyToOne
    @JoinColumn(name = "responder_id", nullable = false)
    private User responder;

    @Column(nullable = false)
    private boolean isCompleted;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HelpRequest getHelpRequest() {
        return helpRequest;
    }

    public void setHelpRequest(HelpRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    public User getResponder() {
        return responder;
    }

    public void setResponder(User responder) {
        this.responder = responder;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
