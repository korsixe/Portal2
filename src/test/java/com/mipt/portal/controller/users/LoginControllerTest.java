package com.mipt.portal.controller.users;

import com.mipt.portal.entity.User;
import com.mipt.portal.enums.Role;
import com.mipt.portal.service.AuditService;
import com.mipt.portal.service.CustomUserDetailsService;
import com.mipt.portal.service.UserService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserService userService;
  @MockBean private CustomUserDetailsService userDetailsService;
  @MockBean private AuditService auditService;

  private User userWithRole(Role role) {
    User u = new User();
    u.setId(1L);
    u.setEmail("u@phystech.edu");
    Set<Role> roles = new HashSet<>();
    roles.add(role);
    u.setRoles(roles);
    return u;
  }

  @Test
  void showLoginPage_redirects() throws Exception {
    mockMvc.perform(get("/users/login"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  void login_redirectsForUser() throws Exception {
    when(userService.loginUser(anyString(), anyString()))
        .thenReturn(Optional.of(userWithRole(Role.USER)));
    UserDetails details = org.springframework.security.core.userdetails.User.withUsername("u@phystech.edu").password("x").roles("USER").build();
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(details);

    mockMvc.perform(post("/users/login").param("email", "u@phystech.edu").param("password", "p"))
        .andExpect(redirectedUrl("/dashboard"));
  }

  @Test
  void login_redirectsForModerator() throws Exception {
    when(userService.loginUser(anyString(), anyString()))
        .thenReturn(Optional.of(userWithRole(Role.MODERATOR)));
    UserDetails details = org.springframework.security.core.userdetails.User.withUsername("u@phystech.edu").password("x").roles("MODERATOR").build();
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(details);
    mockMvc.perform(post("/users/login").param("email", "u@phystech.edu").param("password", "p"))
        .andExpect(redirectedUrl("/moderator/dashboard"));
  }

  @Test
  void login_redirectsForAdminAndAudits() throws Exception {
    when(userService.loginUser(anyString(), anyString()))
        .thenReturn(Optional.of(userWithRole(Role.ADMIN)));
    UserDetails details = org.springframework.security.core.userdetails.User.withUsername("u@phystech.edu").password("x").roles("ADMIN").build();
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(details);
    mockMvc.perform(post("/users/login").param("email", "u@phystech.edu").param("password", "p"))
        .andExpect(redirectedUrl("/admin/dashboard"));
    verify(auditService).logAdminLogin(anyString(), org.mockito.ArgumentMatchers.eq(true), anyString(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void login_failureForwardsAndAuditsAdminFailure() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.empty());
    when(userService.findUserByEmail("admin@phystech.edu"))
        .thenReturn(Optional.of(userWithRole(Role.ADMIN)));

    mockMvc.perform(post("/users/login").param("email", "admin@phystech.edu").param("password", "wrong"))
        .andExpect(view().name("forward:/login.jsp"));
    verify(auditService).logAdminLogin(anyString(), org.mockito.ArgumentMatchers.eq(false), anyString(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void login_failureForRegularUser_doesNotAudit() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.empty());
    when(userService.findUserByEmail("u@phystech.edu")).thenReturn(Optional.of(userWithRole(Role.USER)));
    mockMvc.perform(post("/users/login").param("email", "u@phystech.edu").param("password", "wrong"))
        .andExpect(view().name("forward:/login.jsp"));
  }

  @Test
  void showRegisterPage_redirects() throws Exception {
    mockMvc.perform(get("/users/register"))
        .andExpect(redirectedUrl("/register"));
  }
}
