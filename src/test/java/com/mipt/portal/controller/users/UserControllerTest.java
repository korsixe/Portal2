package com.mipt.portal.controller.users;

import com.mipt.portal.entity.User;
import com.mipt.portal.service.CustomUserDetailsService;
import com.mipt.portal.service.UserService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserService userService;
  @MockBean private CustomUserDetailsService userDetailsService;

  private User user(long id) {
    User u = new User();
    u.setId(id);
    u.setEmail("u@phystech.edu");
    u.setName("Ivan");
    return u;
  }

  @Test
  void register_success() throws Exception {
    User u = user(1L);
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenReturn(Optional.of(u));
    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"u@phystech.edu","name":"Ivan","password":"p","passwordAgain":"p",
                 "address":"a","studyProgram":"PM","course":3}
                """))
        .andExpect(status().isCreated());
  }

  @Test
  void register_badRequestOnEmpty() throws Exception {
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenReturn(Optional.empty());
    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"u@phystech.edu","name":"Ivan","password":"p","passwordAgain":"p2",
                 "address":"a","studyProgram":"PM","course":3}
                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_badRequestOnIllegalArgument() throws Exception {
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenThrow(new IllegalArgumentException("bad"));
    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"u@phystech.edu","name":"Ivan","password":"p","passwordAgain":"p",
                 "address":"a","studyProgram":"PM","course":3}
                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_unauthorized() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.empty());
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"u@phystech.edu\",\"password\":\"p\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_success() throws Exception {
    User u = user(1L);
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.of(u));
    org.springframework.security.core.userdetails.UserDetails details =
        org.springframework.security.core.userdetails.User.withUsername("u@phystech.edu").password("x").roles("USER").build();
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(details);
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"u@phystech.edu\",\"password\":\"p\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void update_unauthorized() throws Exception {
    mockMvc.perform(put("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"X\",\"studyProgram\":\"PM\",\"course\":3}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void update_unauthorizedDifferentId() throws Exception {
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", user(2L));
    mockMvc.perform(put("/api/users/1").session(s)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"X\",\"studyProgram\":\"PM\",\"course\":3}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void update_success() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.updateUser(any())).thenReturn(Optional.of(u));
    mockMvc.perform(put("/api/users/1").session(s)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"X","studyProgram":"PM","course":3,
                 "address":{"fullAddress":"a","city":"M","street":"s","houseNumber":"1","building":"A"}}
                """))
        .andExpect(status().isOk());
  }

  @Test
  void update_badRequestWhenEmptyResult() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.updateUser(any())).thenReturn(Optional.empty());
    mockMvc.perform(put("/api/users/1").session(s)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"X\",\"studyProgram\":\"PM\",\"course\":3}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_badRequestOnValidationError() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.updateUser(any())).thenThrow(new IllegalArgumentException("Имя плохое"));
    mockMvc.perform(put("/api/users/1").session(s)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\",\"studyProgram\":\"PM\",\"course\":3}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_emailValidationError() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.updateUser(any())).thenThrow(new IllegalArgumentException("Почта плохая"));
    mockMvc.perform(put("/api/users/1").session(s)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Ivan\",\"studyProgram\":\"PM\",\"course\":3}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAllUsers_returnsList() throws Exception {
    when(userService.getAllUsers()).thenReturn(List.of(user(1L)));
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk());
  }

  @Test
  void getCurrentUser_unauthorized() throws Exception {
    mockMvc.perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getCurrentUser_returnsUser() throws Exception {
    User u = user(1L);
    when(userService.findUserById(1L)).thenReturn(Optional.of(u));
    org.springframework.security.core.userdetails.UserDetails details =
        org.springframework.security.core.userdetails.User.withUsername("u@phystech.edu").password("x").roles("USER").build();
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(details);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("userId", 1L);
    mockMvc.perform(get("/api/users/me").session(s))
        .andExpect(status().isOk());
  }

  @Test
  void getCurrentUser_unauthorizedWhenNotFound() throws Exception {
    when(userService.findUserById(1L)).thenReturn(Optional.empty());
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("userId", 1L);
    mockMvc.perform(get("/api/users/me").session(s))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void changePassword_unauthorized() throws Exception {
    mockMvc.perform(post("/api/users/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"o\",\"newPassword\":\"n\",\"confirmPassword\":\"n\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void changePassword_success() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.changePassword(any(), anyString(), anyString())).thenReturn(true);
    mockMvc.perform(post("/api/users/change-password").session(s)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"o\",\"newPassword\":\"n\",\"confirmPassword\":\"n\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void changePassword_failure() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.changePassword(any(), anyString(), anyString())).thenReturn(false);
    mockMvc.perform(post("/api/users/change-password").session(s)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"o\",\"newPassword\":\"n\",\"confirmPassword\":\"n\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteAccount_unauthorized() throws Exception {
    mockMvc.perform(delete("/api/users/delete-account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"password\":\"p\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deleteAccount_success() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.deleteAccount(any(), anyString())).thenReturn(true);
    mockMvc.perform(delete("/api/users/delete-account").session(s)
            .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"p\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void deleteAccount_failure() throws Exception {
    User u = user(1L);
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    when(userService.deleteAccount(any(), anyString())).thenReturn(false);
    mockMvc.perform(delete("/api/users/delete-account").session(s)
            .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"p\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void logout_invalidatesSession() throws Exception {
    mockMvc.perform(post("/api/users/logout"))
        .andExpect(status().isOk());
  }
}
