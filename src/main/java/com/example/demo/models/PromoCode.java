package com.example.demo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // Сам промокод

    private String description; // Описание промокода
    private String company; // Название компании
    private int cost; // Стоимость в карме

    @Transient // Это поле не сохраняется в БД, оно будет использоваться только в процессе
    private String userPromoCode; // Промокод, который пользователь получит

    public PromoCode() {}

    public PromoCode(String code, String description, String company, int cost) {
        this.code = code;
        this.description = description;
        this.company = company;
        this.cost = cost;
    }

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getUserPromoCode() {
        return userPromoCode;
    }

    public void setUserPromoCode(String userPromoCode) {
        this.userPromoCode = userPromoCode;
    }
}
