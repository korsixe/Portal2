package com.mipt.portal.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.portal.repository.UserRepository;
import com.mipt.portal.support.E2ETest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end smoke test demonstrating the lecture's recipe:
 * full HTTP → controller → service → real PostgreSQL flow + DB-state assertion.
 *
 * <p>Tagged {@code e2e}; opt in with {@code mvn test -Dgroups=e2e}. Requires a running
 * Docker daemon for Testcontainers.</p>
 */
@E2ETest
class UserRegistrationE2ETest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private ObjectMapper objectMapper;

  @AfterEach
  void cleanup() {
    userRepository.deleteAll();
  }

  @Test
  void registerUser_createsRowInPostgres() throws Exception {
    String body = """
        {
          "email": "e2e@phystech.edu",
          "name": "Ivan",
          "password": "Password1!",
          "passwordAgain": "Password1!",
          "address": "Москва",
          "studyProgram": "PM",
          "course": 3
        }
        """;

    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("e2e@phystech.edu"));

    assertThat(userRepository.findByEmail("e2e@phystech.edu"))
        .as("Зарегистрированный пользователь должен существовать в Postgres")
        .isPresent();
  }
}
