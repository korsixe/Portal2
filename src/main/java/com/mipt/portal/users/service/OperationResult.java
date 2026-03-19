package com.mipt.portal.users.service;

public class OperationResult<T> {

  private final boolean success;
  private final String message;
  private final T data;

  public OperationResult(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  public static <T> OperationResult<T> success(String message, T data) {
    return new OperationResult<>(true, message, data);
  }

  public static <T> OperationResult<T> success(String message) {
    return new OperationResult<>(true, message, null);
  }

  public static <T> OperationResult<T> error(String message) {
    return new OperationResult<>(false, message, null);
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }
}