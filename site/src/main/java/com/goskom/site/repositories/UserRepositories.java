package com.goskom.site.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goskom.site.entities.User;
@Repository
public interface  UserRepositories extends JpaRepository<User, Long> {
    public User findByEmail(String email);
}
