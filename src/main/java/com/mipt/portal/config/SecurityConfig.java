package com.mipt.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
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
            // Публичные страницы
            .requestMatchers(
                new AntPathRequestMatcher("/"),
                new AntPathRequestMatcher("/index.jsp"),
                new AntPathRequestMatcher("/login.jsp"),
                new AntPathRequestMatcher("/register.jsp"),
                new AntPathRequestMatcher("/home.jsp"),
                new AntPathRequestMatcher("/*.jsp")
            ).permitAll()
            // Защищенные страницы (требуют аутентификации)
            .requestMatchers(
                new AntPathRequestMatcher("/dashboard.jsp"),
                new AntPathRequestMatcher("/edit-profile.jsp"),
                new AntPathRequestMatcher("/create-ad.jsp"),
                new AntPathRequestMatcher("/edit-ad.jsp"),
                new AntPathRequestMatcher("/delete-account-handler.jsp")
            ).authenticated()
            .anyRequest().permitAll()
        )
        .formLogin(form -> form
            .loginPage("/login.jsp")
            .loginProcessingUrl("/users/login")
            .defaultSuccessUrl("/dashboard.jsp", true)
            .failureUrl("/login.jsp?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
        );

    return http.build();
  }
}