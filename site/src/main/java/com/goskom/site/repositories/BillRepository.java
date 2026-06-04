package com.goskom.site.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goskom.site.entities.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    // Поиск квитанций, принадлежащих конкретному пользователю
    List<Bill> findByUserId(Long userId);
}