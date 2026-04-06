package com.goskom.site.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig
{   
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }  
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http
            .csrf(csrf -> csrf.disable()) // Отключаем защиту CSRF, чтобы работали POST-запросы
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Разрешаем доступ ко всем страницам без логина
            )
            .formLogin(form -> form.disable()) // Убираем форму логина
            .httpBasic(basic -> basic.disable()); // Убираем базовую аутентификацию
        
        return http.build();
    }
}

