package com.mipt.portal.service;

import com.mipt.portal.entity.User;
import com.mipt.portal.enums.Role;
import com.mipt.portal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private CustomUserDetailsService service;

  private User user(String email, Role... roles) {
    User u = new User();
    u.setId(1L);
    u.setEmail(email);
    u.setHashPassword("hash");
    HashSet<Role> rs = new HashSet<>();
    for (Role r : roles) rs.add(r);
    u.setRoles(rs);
    return u;
  }

  @Test
  void loadUserByUsername_returnsPrincipal() {
    User u = user("u@phystech.edu", Role.USER);
    when(userRepository.findByEmail("u@phystech.edu")).thenReturn(Optional.of(u));
    UserDetails details = service.loadUserByUsername("u@phystech.edu");
    assertThat(details.getUsername()).isEqualTo("u@phystech.edu");
    assertThat(details.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_USER");
  }

  @Test
  void loadUserByUsername_throwsWhenAbsent() {
    when(userRepository.findByEmail("nope")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.loadUserByUsername("nope"))
        .isInstanceOf(UsernameNotFoundException.class);
  }

  @Test
  void loadUserByUsername_wrapsRepoException() {
    when(userRepository.findByEmail("nope")).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.loadUserByUsername("nope"))
        .isInstanceOf(UsernameNotFoundException.class);
  }

  @Test
  void principal_authoritiesFallbackToUserWhenNullRoles() {
    User u = user("u@phystech.edu");
    u.setRoles(null);
    CustomUserDetailsService.CustomUserPrincipal principal =
        new CustomUserDetailsService.CustomUserPrincipal(u);
    assertThat(principal.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_USER");
  }

  @Test
  void principal_authoritiesFromRoles() {
    User u = user("u@phystech.edu", Role.MODERATOR, Role.ADMIN);
    CustomUserDetailsService.CustomUserPrincipal principal =
        new CustomUserDetailsService.CustomUserPrincipal(u);
    assertThat(principal.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_MODERATOR", "ROLE_ADMIN");
    assertThat(principal.isModerator()).isTrue();
  }

  @Test
  void principal_basics() {
    User u = user("u@phystech.edu", Role.USER);
    CustomUserDetailsService.CustomUserPrincipal principal =
        new CustomUserDetailsService.CustomUserPrincipal(u);
    assertThat(principal.getPassword()).isEqualTo("hash");
    assertThat(principal.getUsername()).isEqualTo("u@phystech.edu");
    assertThat(principal.isAccountNonExpired()).isTrue();
    assertThat(principal.isCredentialsNonExpired()).isTrue();
    assertThat(principal.isAccountNonLocked()).isTrue();
    assertThat(principal.isEnabled()).isTrue();
    assertThat(principal.getUser()).isSameAs(u);
    assertThat(principal.getUserId()).isEqualTo(1L);
  }

  @Test
  void principal_lockedWhenBanned() {
    User u = user("u@phystech.edu", Role.USER);
    u.setBannedUntil(Instant.now().plusSeconds(60));
    CustomUserDetailsService.CustomUserPrincipal principal =
        new CustomUserDetailsService.CustomUserPrincipal(u);
    assertThat(principal.isAccountNonLocked()).isFalse();
    assertThat(principal.isEnabled()).isFalse();
  }

  @Test
  void principal_lockedWhenFrozen() {
    User u = user("u@phystech.edu", Role.USER);
    u.setFrozenUntil(Instant.now().plusSeconds(60));
    CustomUserDetailsService.CustomUserPrincipal principal =
        new CustomUserDetailsService.CustomUserPrincipal(u);
    assertThat(principal.isAccountNonLocked()).isFalse();
    assertThat(principal.isEnabled()).isTrue();
  }
}
