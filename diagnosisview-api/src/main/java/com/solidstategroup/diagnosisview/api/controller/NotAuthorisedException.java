package com.solidstategroup.diagnosisview.api.controller;

public class NotAuthorisedException extends RuntimeException {
    public NotAuthorisedException(String s) {
        super(s);
    }
}
