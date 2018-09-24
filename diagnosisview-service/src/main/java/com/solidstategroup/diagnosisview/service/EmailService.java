package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.User;

import java.io.IOException;

/**
 * Service to handle sending emails to sers when resetting a password
 */
public interface EmailService {

    void sendForgottenPasswordEmail(final User user, final String resetCode) throws IOException;


    void sendFeedback(String subject, String message);


}
