package com.solidstategroup.diagnosisview.exceptions;

/**
 * AppleReceiptValidationFailedException to handle apple receipt validation errors
 */
public class AppleReceiptValidationFailedException extends Exception {

  public AppleReceiptValidationFailedException() {
    super();
  }

  public AppleReceiptValidationFailedException(String message) {
    super(message);
  }

  public AppleReceiptValidationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppleReceiptValidationFailedException(Throwable cause) {
    super(cause);
  }
}
