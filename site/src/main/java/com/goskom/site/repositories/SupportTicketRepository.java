package com.goskom.site.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goskom.site.entities.SupportTicket;
import com.goskom.site.entities.User;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    // Поиск всех заявок конкретного жильца от новых к старым
    List<SupportTicket> findByUserOrderByCreatedAtDesc(User user);
}