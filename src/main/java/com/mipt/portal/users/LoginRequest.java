package com.mipt.portal.users;

import lombok.Data;

@Data
public class LoginRequest {
  private String email;
  private String password;
}
