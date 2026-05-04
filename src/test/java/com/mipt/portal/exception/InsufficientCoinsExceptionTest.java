package com.mipt.portal.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InsufficientCoinsExceptionTest {

  @Test
  void exposesFieldsAndMessage() {
    InsufficientCoinsException ex = new InsufficientCoinsException(5L, 3, 10);
    assertThat(ex.getUserId()).isEqualTo(5L);
    assertThat(ex.getBalance()).isEqualTo(3);
    assertThat(ex.getRequired()).isEqualTo(10);
    assertThat(ex.getMessage()).contains("userId=5", "balance=3", "required=10");
  }
}
