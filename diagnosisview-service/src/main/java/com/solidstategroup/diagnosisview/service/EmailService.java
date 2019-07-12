package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.User;

import java.io.IOException;

/**
 * Service to handle sending emails to sers when resetting a password
 */
public interface EmailService {

    /**
     * Send a forgotten password email to a user
     * @param user the user requesting the forgotten password email
     * @param resetCode the generated code to be added to the email
     * @throws IOException
     */
    void sendForgottenPasswordEmail(final User user, final String resetCode) throws IOException;

    /**
     * Send feedback to the DV team
     * @param user the user sending the feedback, if logged in
     * @param message the message being sent
     */
    void sendFeedback(final User user, final String message);


}
