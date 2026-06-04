package com.goskom.site.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Например: "Протекает труба в подвале"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // Подробное описание проблемы

    @Column(nullable = false)
    private String category; // Сантехника, Электрика, Двор и т.д.

    @Column(nullable = false)
    private String status = "НОВАЯ"; // НОВАЯ, В_РАБОТЕ, РЕШЕНА

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Кто создал заявку

    @Column(name = "created_at", nullable = false, updatable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private String address;
    public SupportTicket() {}

    public SupportTicket(String title, String description, String category, User user, String address) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.user = user;
        this.address = address;
    }

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}