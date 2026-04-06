package com.goskom.site.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
    public class User
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(name = "name", nullable = false)
        private String name;
        @Column(name = "email", nullable = false, unique = true)
        private String email;
        @Column(name = "password", nullable = false)
        private String password;
        @Column(name = "createdAt", nullable = false)
        private LocalDateTime createdAt;

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
    }
