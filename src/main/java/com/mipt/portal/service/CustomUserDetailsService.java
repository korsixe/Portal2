package com.mipt.portal.service;

import com.mipt.portal.entity.User;
import com.mipt.portal.repository.UserRepository;
import com.mipt.portal.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для загрузки пользователей для Spring Security.
 * Реализует UserDetailsService для интеграции с системой аутентификации.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                log.warn("User not found: {}", email);
                throw new UsernameNotFoundException("Пользователь не найден: " + email);
            }

            User user = userOpt.get();
            log.debug("User loaded: {} with roles: {}", email, user.getRoles());

            return new CustomUserPrincipal(user);

        } catch (Exception e) {
            log.error("Error loading user: {}", e.getMessage(), e);
            throw new UsernameNotFoundException("Ошибка при загрузке пользователя: " + email);
        }
    }

    /**
     * Реализация UserDetails для пользователя
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            Set<Role> roles = user.getRoles();
            if (roles == null || roles.isEmpty()) {
                return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                    .collect(Collectors.toSet());
        }

        @Override
        public String getPassword() {
            return user.getHashPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return !user.isBanned() && !user.isFrozen();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return !user.isBanned();
        }

        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getId();
        }

        public boolean isModerator() {
            return user.isModerator();
        }
    }
}
