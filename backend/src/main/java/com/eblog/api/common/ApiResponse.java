package com.eblog.api.common;

public class ApiResponse<T> {
  private boolean success;
  private T data;
  private ApiError error;

  public static <T> ApiResponse<T> ok(T data) {
    ApiResponse<T> res = new ApiResponse<T>();
    res.success = true;
    res.data = data;
    return res;
  }

  public static <T> ApiResponse<T> fail(String code, String message) {
    ApiResponse<T> res = new ApiResponse<T>();
    res.success = false;
    res.error = new ApiError(code, message);
    return res;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public ApiError getError() {
    return error;
  }

  public void setError(ApiError error) {
    this.error = error;
  }
}
