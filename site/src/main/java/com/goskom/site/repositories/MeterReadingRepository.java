package com.goskom.site.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goskom.site.entities.BillType;
import com.goskom.site.entities.MeterReading;
import com.goskom.site.entities.User;
@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    // Этот метод нужен админке, чтобы вытащить последнее показание для расхода
    List<MeterReading> findByUserOrderByCreatedAtDesc(User user);
    
    // 2. Метод для истории с фильтрацией по типу услуги:
    List<MeterReading> findByUserAndTypeOrderByCreatedAtDesc(User user, BillType type);
    MeterReading findFirstByUserAndTypeOrderByCreatedAtDesc(User user, BillType type);
}