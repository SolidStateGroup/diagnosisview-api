package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.exceptions.ImageIOException;
import com.solidstategroup.diagnosisview.exceptions.ImageNotFoundException;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityExistsException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Provides controller advice (Exception handling etc...)
 */
@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(BadRequestException.class)
  public ErrorMessage badRequestException(BadRequestException bre) {
    return new ErrorMessage(bre.getMessage());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceNotFoundException.class)
  public ErrorMessage notFoundException(ResourceNotFoundException e) {
    return new ErrorMessage(e.getMessage());
  }


  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(Exception.class)
  public ErrorMessage AuthExcepionHandler(BadCredentialsException bce) {

    return new ErrorMessage(bce.getMessage());
  }

  /**
   * Handles exceptions thrown when finding images. Ensures no JSON is serialized in the response.
   */
  @ExceptionHandler(ImageNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void imageNotFoundException() {
  }

  /**
   * Handle exception thrown when retrieving images. Ensures no JSON is serialized in the response.
   */
  @ExceptionHandler(ImageIOException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void imageIoException() {
  }

  @ResponseStatus(value = HttpStatus.CONFLICT)
  @ExceptionHandler(EntityExistsException.class)
  public ErrorMessage handleEntityException(EntityExistsException e) {
    log.error("Handling Entity Exception: ", e);
    return new ErrorMessage(e.getMessage());
  }

  // Validation exception
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> validation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return errors;
  }

  @Data
  @AllArgsConstructor
  public static class ErrorMessage {

    private String message;
  }
}
