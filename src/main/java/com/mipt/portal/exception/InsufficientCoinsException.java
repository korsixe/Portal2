package com.mipt.portal.exception;

public class InsufficientCoinsException extends RuntimeException {
  private final Long userId;
  private final int balance;
  private final int required;

  public InsufficientCoinsException(Long userId, int balance, int required) {
    super("Insufficient coins for userId=" + userId + ": balance=" + balance + ", required=" + required);
    this.userId = userId;
    this.balance = balance;
    this.required = required;
  }

  public Long getUserId() {
    return userId;
  }

  public int getBalance() {
    return balance;
  }

  public int getRequired() {
    return required;
  }
}

