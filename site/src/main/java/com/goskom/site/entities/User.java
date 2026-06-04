package com.goskom.site.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
@Entity
@Table(name = "users")
    public class User
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(name = "address") // или nullable = false, если адрес обязателен
        private String address;
        @Column(name = "name", nullable = false)
        private String name;
        @Column(name = "email", nullable = false, unique = true)
        private String email;
        @Column(name = "password", nullable = false)
        private String password;
        @Column(name = "createdAt", nullable = false)
        
        private LocalDateTime createdAt;
        private String avatarPath;
        private Double coldWater = 0.0;
        private Double hotWater = 0.0;
        private Integer electricity = 0;
        private Double gas = 0.0;
        private Double heating = 0.0    ;
        @OneToMany(mappedBy = "user")
        private List<Bill> bills;
        private String role = "USER";

        public List<Bill> getBills() { return bills; }
        public void setBills(List<Bill> bills) { this.bills = bills; }
        public User() {}

        public Long getId() {return id;}
        public void setId(Long id) {this.id = id;}
        
        public String getName() {return name;}
        public void setName(String name) {this.name = name;}
        
        public String getEmail() {return email;}
        public void setEmail(String email) {this.email = email;}

        public String getPassword() {return password;}
        public void setPassword(String password) {this.password = password;}

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        @PrePersist
        protected void onCreate() {
            this.createdAt = java.time.LocalDateTime.now();
        }
        public String getAvatarPath() {
            return avatarPath;
        }
        public void setAvatarPath(String avatarPath) {
            this.avatarPath = avatarPath;
        }
        public Double getColdWater() { return coldWater; }
        public void setColdWater(Double coldWater) { this.coldWater = coldWater; }
        public Double getHotWater() { return hotWater; }
        public void setHotWater(Double hotWater) { this.hotWater = hotWater; }
        public Integer getElectricity() { return electricity; }
        public void setElectricity(Integer electricity) { this.electricity = electricity; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Double getGas() {
            return gas;
        }
        public void setGas(Double gas) {
            this.gas = gas;
        }
        public Double getHeating() {
            return heating;
        }

        public void setHeating(Double heating) {
            this.heating = heating;
        }
        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
