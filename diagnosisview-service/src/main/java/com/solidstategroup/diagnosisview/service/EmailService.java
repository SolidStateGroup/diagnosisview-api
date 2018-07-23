package com.solidstategroup.diagnosisview.service;

/**
 * Service to handle building sector DTOs from data retrieved from KeyedIn.
 */
public interface EmailService {

    /**
     * Sends an alert email when a system issue occurs.
     *
     * @param message - Exception message to send
     */
    void sendAlertEmail(final String message);
}
