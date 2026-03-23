package com.mipt.portal.dto;

import lombok.Data;

@Data
public class RegisterRequest {
  private String email;
  private String name;
  private String password;
  private String passwordAgain;
  private String address;
  private String studyProgram;
  private int course;
}
