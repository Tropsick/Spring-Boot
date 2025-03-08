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
    private String companyName; // Название компании
    private int price; // Стоимость в карме
    private boolean isActive; // Активен ли промокод

    public PromoCode() {}

    // Новый конструктор для фильтрации активных промокодов без кода
    public PromoCode(Long id, String code, int price, String description, String companyName, boolean isActive) {
        this.id = id;
        this.code = code;
        this.price = price;
        this.description = description;
        this.companyName = companyName;
        this.isActive = isActive;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
