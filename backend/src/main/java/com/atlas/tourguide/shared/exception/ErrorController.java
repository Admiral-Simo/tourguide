package com.atlas.tourguide.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@ControllerAdvice
@Slf4j
public class ErrorController {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    List<ApiErrorResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new ApiErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
            .collect(Collectors.toList());

    ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .errors(errors)
            .build();
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
    ApiErrorResponse error = ApiErrorResponse.builder()
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message(ex.getMessage()).build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    ApiErrorResponse error = ApiErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value())
        .message(ex.getMessage()).build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException ex) {
    ApiErrorResponse error = ApiErrorResponse.builder().status(HttpStatus.CONFLICT.value())
        .message(ex.getMessage()).build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
      BadCredentialsException ex) {
    ApiErrorResponse error = ApiErrorResponse.builder().status(HttpStatus.UNAUTHORIZED.value())
        .message("Incorrect username or password.").build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(
          EntityNotFoundException ex) {
    ApiErrorResponse error = ApiErrorResponse.builder().status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage()).build(); // Use the actual message from the exception

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }
}
