package com.goskom.site.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.goskom.site.entities.User;
import com.goskom.site.repositories.UserRepositories;
@Service
public class UserService
{
    @Autowired
    private UserRepositories userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);

        if (user != null) { 
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return false;
    }
    
}