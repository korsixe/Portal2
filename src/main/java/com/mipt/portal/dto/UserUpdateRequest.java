package com.mipt.portal.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
  private String name;
  private AddressDTO address;
  private String studyProgram;
  private int course;

  @Data
  public static class AddressDTO {
    private String fullAddress;
    private String city;
    private String street;
    private String houseNumber;
    private String building;
  }
}