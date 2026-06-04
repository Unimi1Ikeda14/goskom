package com.goskom.site.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber; // Например, "ЖКХ-100234"

    private String address;
    private double area; // Площадь квартиры для расчета отопления

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Владелец счета

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Meter> meters;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<Meter> getMeters() { return meters; }
    public void setMeters(List<Meter> meters) { this.meters = meters; }
}