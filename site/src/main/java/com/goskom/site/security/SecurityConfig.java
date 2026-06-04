package com.goskom.site.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/login", "/register", "/error")) // Отключаем CSRF для этих путей
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers("/css/**", "/js/**", "/","/news", "/news/**", "/error", "/outages").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .anyRequest().permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL, на который отправляется форма
                .logoutSuccessUrl("/") // Куда перенаправить после выхода (на главную)
                .invalidateHttpSession(true) // Удалить сессию из памяти сервера
                .clearAuthentication(true) // Очистить контекст безопасности
                .deleteCookies("JSESSIONID") // Стереть куки авторизации в браузере
                .permitAll()
            )
            .formLogin(form -> form.disable()) 
            .httpBasic(basic -> basic.disable()); 
        return http.build();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Этот метод полностью отключает ЛЮБЫЕ фильтры безопасности (включая CSRF и авторизацию) для указанных путей
        return (web) -> web.ignoring().requestMatchers("/error", "/css/**", "/js/**");
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}