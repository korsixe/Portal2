package com.mipt.portal.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

  @Test
  void constructor_withFullAddress() {
    Address a = new Address("Москва, ул. Тверская");
    assertThat(a.getFullAddress()).isEqualTo("Москва, ул. Тверская");
  }

  @Test
  void getYandexMapsUrl_fromFullAddress() {
    Address a = new Address("Москва Тверская");
    assertThat(a.getYandexMapsUrl()).isEqualTo("https://maps.yandex.ru/?text=Москва+Тверская");
  }

  @Test
  void getYandexMapsUrl_buildsFromComponents() {
    Address a = new Address();
    a.setCity("Москва");
    a.setStreet("Тверская");
    a.setHouseNumber("1");
    a.setBuilding("A");
    String url = a.getYandexMapsUrl();
    assertThat(url).startsWith("https://maps.yandex.ru/?text=");
    assertThat(url).contains("Москва");
    assertThat(url).contains("Тверская");
  }

  @Test
  void getYandexMapsUrl_returnsBaseWhenAllEmpty() {
    Address a = new Address();
    assertThat(a.getYandexMapsUrl()).isEqualTo("https://maps.yandex.ru/");
  }

  @Test
  void getYandexMapsUrlWithCoordinates_usesCoords() {
    Address a = new Address();
    a.setLatitude(55.75);
    a.setLongitude(37.62);
    assertThat(a.getYandexMapsUrlWithCoordinates()).contains("pt=37");
  }

  @Test
  void getYandexMapsUrlWithCoordinates_fallsBackWhenNullCoords() {
    Address a = new Address("ул. Ленина");
    assertThat(a.getYandexMapsUrlWithCoordinates()).contains("text=");
  }

  @Test
  void getFormattedAddress_returnsFullAddress() {
    Address a = new Address("Полный адрес");
    assertThat(a.getFormattedAddress()).isEqualTo("Полный адрес");
  }

  @Test
  void getFormattedAddress_buildsFromParts() {
    Address a = new Address();
    a.setCity("Москва");
    a.setStreet("Ленина");
    a.setHouseNumber("10");
    a.setBuilding("2");
    assertThat(a.getFormattedAddress())
        .isEqualTo("Москва, ул. Ленина, д. 10/2");
  }

  @Test
  void getFormattedAddress_handlesEmpty() {
    Address a = new Address();
    assertThat(a.getFormattedAddress()).isEmpty();
  }
}
