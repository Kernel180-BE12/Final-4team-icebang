package site.icebang.global.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

import site.icebang.common.dto.ApiResponse;
import site.icebang.common.exception.DuplicateDataException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<String> handleValidation(MethodArgumentNotValidException ex) {
    String detail = ex.getBindingResult().toString();
    return ApiResponse.error("Validation failed: " + detail, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<String> handleGeneric(Exception ex) {
    log.error(ex.getMessage(), ex);
    return ApiResponse.error("Internal error: ", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiResponse<String> handleNotFound(NoResourceFoundException ex) {
    return ApiResponse.error("Notfound: " + ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<String> handleAuthentication(AuthenticationException ex) {
    return ApiResponse.error("Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiResponse<String> handleAccessDenied(AccessDeniedException ex) {
    return ApiResponse.error("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(DuplicateDataException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<String> handleDuplicateData(DuplicateDataException ex) {
    log.warn(ex.getMessage(), ex);
    return ApiResponse.error("Duplicate: " + ex.getMessage(), HttpStatus.CONFLICT);
  }
    @ExceptionHandler({PasswordMismatchException.class, InvalidPasswordException.class}) //추가
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handlePasswordException(RuntimeException ex) {
        return ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
