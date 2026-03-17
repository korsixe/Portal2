package com.mipt.portal.user;

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
