package com.goskom.site.entities;

import jakarta.persistence.Entity;
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

    private String title;      // Название (напр. "Оплата ГВС")
    private Double amount;     // Сумма
    private boolean isPaid;    // Статус оплаты

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;         

    public Bill() {}
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    public boolean isPaid() {
        return isPaid;
    }
    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }
}
