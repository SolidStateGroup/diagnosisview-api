package com.solidstategroup.diagnosisview.exceptions;


/**
 * Created by Pavlo Maksymchuk.
 */
public class UsernameTakenException extends RuntimeException {

  public UsernameTakenException() {
    super("The username already exists. Please try another one");
  }
}
