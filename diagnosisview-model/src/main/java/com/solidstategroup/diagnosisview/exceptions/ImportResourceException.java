package com.solidstategroup.diagnosisview.exceptions;

/**
 * ImportResourceException exception thrown on importing data from external sources.
 */
public class ImportResourceException extends Exception {
    public ImportResourceException(String message) {
        super(message);
    }
}
