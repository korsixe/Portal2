package com.mipt.portal.address;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable
public class Address {

  @Column(name = "full_address")
  private String fullAddress;

  @Column(name = "city")
  private String city;               // Город

  @Column(name = "street")
  private String street;             // Улица

  @Column(name = "house_number")
  private String houseNumber;        // Номер дома

  @Column(name = "building")
  private String building;           // Корпус/строение

  @Column(name = "apartment")
  private String apartment;

  // Координаты для карт
  @Column(name = "latitude")
  private Double latitude;           // Широта

  @Column(name = "longitude")
  private Double longitude;          // Долгота

  // Дополнительная информация
  @Column(name = "entrance")
  private String entrance;           // Подъезд

  @Column(name = "floor")
  private String floor;              // Этаж

  public Address(String fullAddress) {
    this.fullAddress = fullAddress;
  }

  /**
   * Получает URL для Яндекс.Карт на основе адреса
   */
  public String getYandexMapsUrl() {
    if (fullAddress != null && !fullAddress.isEmpty()) {
      return "https://maps.yandex.ru/?text=" + fullAddress.replace(" ", "+");
    }

    StringBuilder addressBuilder = new StringBuilder();
    if (city != null) addressBuilder.append(city);
    if (street != null) addressBuilder.append(", ").append(street);
    if (houseNumber != null) addressBuilder.append(", ").append(houseNumber);
    if (building != null && !building.isEmpty()) addressBuilder.append("/").append(building);
    if (apartment != null) addressBuilder.append(", кв. ").append(apartment);

    String address = addressBuilder.toString();
    if (!address.isEmpty()) {
      return "https://maps.yandex.ru/?text=" + address.replace(" ", "+");
    }

    return "https://maps.yandex.ru/";
  }

  /**
   * Получает URL для Яндекс.Карт с координатами
   */
  public String getYandexMapsUrlWithCoordinates() {
    if (latitude != null && longitude != null) {
      return String.format("https://maps.yandex.ru/?pt=%f,%f&z=17", longitude, latitude);
    }
    return getYandexMapsUrl();
  }


  public String getFormattedAddress() {
    if (fullAddress != null && !fullAddress.isEmpty()) {
      return fullAddress;
    }

    StringBuilder formatted = new StringBuilder();
    if (city != null) formatted.append(city);
    if (street != null) formatted.append(", ул. ").append(street);
    if (houseNumber != null) formatted.append(", д. ").append(houseNumber);
    if (building != null && !building.isEmpty()) formatted.append("/").append(building);
    if (apartment != null && !apartment.isEmpty()) formatted.append(", кв. ").append(apartment);
    if (entrance != null && !entrance.isEmpty()) formatted.append(", подъезд ").append(entrance);
    if (floor != null && !floor.isEmpty()) formatted.append(", этаж ").append(floor);

    return formatted.toString();
  }
}