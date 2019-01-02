package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Provides controller advice (Exception handling etc...)
 */
@RestControllerAdvice
public class ApiControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(BadRequestException.class)
    public ErrorMessage badRequestException(BadRequestException bre) {
        return new ErrorMessage(bre.getMessage());
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(Exception.class)
    public ErrorMessage AuthExcepionHandler(BadCredentialsException bce) {

        return new ErrorMessage(bce.getMessage());
    }

    @Data
    @AllArgsConstructor
    public static class ErrorMessage {
        private String message;
    }
}
