package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Provides controller advice (Exception handling etc...)
 */
@ControllerAdvice
public class ApiControllerAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(BadRequestException.class)
    public ErrorMessage badRequestException(BadRequestException bre) {
        return new ErrorMessage(bre.getMessage());
    }

    @Data
    @AllArgsConstructor
    public static class ErrorMessage {
        private String message;
    }
}
