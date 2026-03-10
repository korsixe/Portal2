package com.mipt.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Включаем поддержку @PreAuthorize
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz
            // Публичные эндпоинты
            .requestMatchers("/api/users/register", "/api/users/login").permitAll()
            // Системные эндпоинты для инициализации
            .requestMatchers("/api/system/**").permitAll()
            // Администраторские эндпоинты
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            // Модераторские эндпоинты
            .requestMatchers("/api/moderation/**").hasAnyRole("MODERATOR", "ADMIN")
            // Остальные запросы требуют аутентификации
            .anyRequest().authenticated()
        )
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable());

    return http.build();
  }
}
