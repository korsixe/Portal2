package com.mipt.portal.config;

import com.mipt.portal.users.Role;
import com.mipt.portal.users.User;
import com.mipt.portal.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.mipt.portal.entity.Address;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createTestUser("admin@mipt.ru", "admin", Role.ADMIN);
        createTestUser("moderator@mipt.ru", "moderator", Role.MODERATOR);
        createTestUser("user@mipt.ru", "user", Role.USER);
    }

    private void createTestUser(String email, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            log.info("Creating user: {} with role: {}", email, role);
            User user = new User();
            user.setEmail(email);
            user.setName("Test " + role.name());
            
            String salt = UUID.randomUUID().toString().substring(0, 10);
            user.setSalt(salt);
            user.setHashPassword(passwordEncoder.encode(password + salt));
            
            user.addRole(role);
            user.setRating(3.0);
            user.setCoins(100);
            
            Address address = new Address();
            address.setFullAddress("Moscow, Kremlin");
            address.setCity("Moscow");
            address.setStreet("Kremlin");
            user.setAddress(address);
            
            userRepository.save(user);
        } else {
             log.info("User {} already exists", email);
        }
    }
}

