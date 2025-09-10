package site.icebang.common.dto;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ApiResponse<T> {
  private boolean success;
  private T data;
  private String message;
  private HttpStatus status; // HttpStatus로 변경

  public ApiResponse() {}

  public ApiResponse(boolean success, T data, String message, HttpStatus status) {
    this.success = success;
    this.data = data;
    this.message = message;
    this.status = status;
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, "OK", HttpStatus.OK);
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, data, message, HttpStatus.OK);
  }

  public static <T> ApiResponse<T> success(T data, String message, HttpStatus status) {
    return new ApiResponse<>(true, data, message, status);
  }

  public static <T> ApiResponse<T> error(String message, HttpStatus status) {
    return new ApiResponse<>(false, null, message, status);
  }
}
