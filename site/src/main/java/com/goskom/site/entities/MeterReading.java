package com.goskom.site.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "meter_readings")
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Должно быть созвучно с findByUser...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillType type;

    @Column(nullable = false)
    private Double value;

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Должно быть созвучно с ...OrderByCreatedAtDesc
    @Column(name = "consumption")
    private Double consumption;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Геттеры и Сеттеры ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BillType getType() { return type; }
    public void setType(BillType type) { this.type = type; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Double getConsumption() { return consumption; }
    public void setConsumption(Double consumption) { this.consumption = consumption; }
}