package com.solidstategroup.diagnosisview.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String s) {
        super(s);
    }
}
