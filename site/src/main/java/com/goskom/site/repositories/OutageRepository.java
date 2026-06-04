package com.goskom.site.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goskom.site.entities.Outage;

@Repository
public interface OutageRepository extends JpaRepository<Outage, Long> {
    // Получить все отключения, отсортированные по дате создания (сначала новые)
    List<Outage> findAllByOrderByCreatedAtDesc();
}