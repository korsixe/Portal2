package com.mipt.portal.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Value("${app.security.pepper}")
    private String pepper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(12);

        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                String pepperedPassword = rawPassword.toString() + pepper;
                return bCrypt.encode(pepperedPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                String pepperedPassword = rawPassword.toString() + pepper;
                return bCrypt.matches(pepperedPassword, encodedPassword);
            }
        };
    }
}