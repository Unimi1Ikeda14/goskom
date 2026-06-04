package com.goskom.site.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goskom.site.entities.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    // Вытащить все новости, сначала новые
    List<News> findAllByOrderByCreatedAtDesc();
}