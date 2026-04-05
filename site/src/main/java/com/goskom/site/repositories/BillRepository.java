package com.goskom.site.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goskom.site.entities.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUserId(Long userId); // Поиск счетов конкретного пользователя
}