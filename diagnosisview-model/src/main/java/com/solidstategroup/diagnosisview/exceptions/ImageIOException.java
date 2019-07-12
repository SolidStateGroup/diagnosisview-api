package com.solidstategroup.diagnosisview.exceptions;

/**
 * Designed to be thrown when an image cannot be read from
 * storage. This exception will be handled differently than
 * most exceptions in the controller layer, as the controller
 * error handlers will ensure not JSON is returned to match
 * the method handlers signature.
 */
public class ImageIOException extends RuntimeException {

    public ImageIOException(Throwable cause) {
        super(cause);
    }
}
