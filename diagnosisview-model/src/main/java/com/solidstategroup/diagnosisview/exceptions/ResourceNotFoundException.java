package com.solidstategroup.diagnosisview.exceptions;

/**
 * ResourceNotFoundException to handle not found resources in the system
 */
public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
