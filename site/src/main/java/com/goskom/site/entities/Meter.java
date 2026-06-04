package com.goskom.site.entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meters")
public class Meter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "Вода холодная", "Вода горячая", "Электричество"
    private double currentReading; // Последнее переданное значение
    private LocalDate lastUpdated;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getCurrentReading() { return currentReading; }
    public void setCurrentReading(double currentReading) { this.currentReading = currentReading; }
    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}