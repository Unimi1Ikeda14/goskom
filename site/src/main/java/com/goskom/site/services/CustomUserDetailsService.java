package com.goskom.site.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.goskom.site.entities.User;
import com.goskom.site.repositories.UserRepositories;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepositories userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Ищем пользователя в БД по email
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь с email " + email + " не найден.");
        }

        // Возвращаем объект UserDetails, который понятен Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}