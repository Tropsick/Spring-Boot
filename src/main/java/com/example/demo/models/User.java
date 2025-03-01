package com.example.demo.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore // Скрываем passwordHash при JSON-ответе
    @Column(nullable = false)
    private String passwordHash;

    @Transient // Это поле не сохраняется в БД
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Видно только при приеме запроса
    private String password;

    private String avatar;
    private int karma;

    public User() {}

    public User(Long id, String username, String passwordHash, String avatar, int karma) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.avatar = avatar;
        this.karma = karma;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }
}
