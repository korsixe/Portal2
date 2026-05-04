package com.mipt.portal.controller.users;

import com.mipt.portal.entity.User;
import com.mipt.portal.service.UserService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserService userService;

  @Test
  void showForm_returnsView() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"));
  }

  @Test
  void register_successAttributesModel() throws Exception {
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenReturn(Optional.of(new User()));
    mockMvc.perform(post("/register")
            .param("email", "u@phystech.edu")
            .param("name", "Ivan")
            .param("password", "Password1!")
            .param("passwordAgain", "Password1!")
            .param("addressFull", "Москва")
            .param("addressCity", "Moscow")
            .param("addressStreet", "Tverskaya")
            .param("addressHouseNumber", "1")
            .param("addressBuilding", "A")
            .param("studyProgram", "PM")
            .param("course", "3"))
        .andExpect(view().name("register"))
        .andExpect(model().attribute("registrationSuccess", true));
  }

  @Test
  void register_failureAttributesModel() throws Exception {
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenReturn(Optional.empty());
    mockMvc.perform(post("/register")
            .param("email", "u@phystech.edu")
            .param("name", "Ivan")
            .param("password", "Password1!")
            .param("passwordAgain", "Password1!")
            .param("studyProgram", "PM"))
        .andExpect(view().name("register"))
        .andExpect(model().attribute("registrationSuccess", false));
  }
}
