package com.goskom.site.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BillType type; // Фиксированный тип услуги

    private double amount; // Сумма платежа
    private boolean paid;  // Статус оплаты

    private LocalDateTime createdAt; // Дата и время выставления счета
    private LocalDateTime paidAt;    // Дата и время оплаты

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Bill() {}

    public Bill(BillType type, double amount, User user) {
        this.type = type;
        this.amount = amount;
        this.user = user;
        this.paid = false;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BillType getType() { return type; }
    public void setType(BillType type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}