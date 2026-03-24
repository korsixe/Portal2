package com.mipt.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
    MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);

    http
        .csrf(AbstractHttpConfigurer::disable)
        .securityContext(sc -> sc
            .securityContextRepository(new HttpSessionSecurityContextRepository())
            .requireExplicitSave(false)
        )
        .authorizeHttpRequests(authz -> authz
            // Публичные страницы
            .requestMatchers(
                mvc.pattern("/"),
                mvc.pattern("/index.jsp"),
                mvc.pattern("/login.jsp"),
                mvc.pattern("/register.jsp"),
                mvc.pattern("/home.jsp"),
                mvc.pattern("/*.jsp"),
                // Разрешаем доступ к нашим контроллерам
                mvc.pattern("/users/login"),
                mvc.pattern("/users/register"),
                mvc.pattern("/custom-login")
            ).permitAll()
            // Защищенные страницы
            .requestMatchers(mvc.pattern("/moderator/**")).hasAnyRole("MODERATOR", "ADMIN")
            .requestMatchers(mvc.pattern("/admin/**")).hasRole("ADMIN")
            .requestMatchers(
                mvc.pattern("/dashboard.jsp"),
                mvc.pattern("/edit-profile.jsp"),
                mvc.pattern("/create-ad.jsp"),
                mvc.pattern("/edit-ad.jsp"),
                mvc.pattern("/delete-account-handler.jsp")
            ).authenticated()
            .anyRequest().permitAll()
        )
        // Отключаем стандартную форму логина Spring Security
        .formLogin(AbstractHttpConfigurer::disable)
        // Отключаем HTTP Basic аутентификацию
        .httpBasic(AbstractHttpConfigurer::disable)
        // Настраиваем logout
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