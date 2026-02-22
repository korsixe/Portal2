package com.mipt.portal.address;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Address {
  private String fullAddress;
  private String city;               // Город
  private String street;             // Улица
  private String houseNumber;        // Номер дома
  private String building;           // Корпус/строение
  private String apartment;

  // Координаты для карт
  private Double latitude;           // Широта
  private Double longitude;          // Долгота

  // Дополнительная информация
  private String entrance;           // Подъезд
  private String floor;              // Этаж

  public Address(String fullAddress) {
    this.fullAddress = fullAddress;
  }

  public void getYandexMapsUrl() {
   // заглушка, переделаем потом на String
  }
}