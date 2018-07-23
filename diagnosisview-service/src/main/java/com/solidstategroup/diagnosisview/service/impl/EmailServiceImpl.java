package com.solidstategroup.diagnosisview.service.impl;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.solidstategroup.diagnosisview.service.EmailService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * {@inheritDoc}.
 */
@Log
@Service
public class EmailServiceImpl implements EmailService {

    // Replace sender@example.com with your "From" address.
    // This address must be verified with Amazon SES.
    @Value("${ALERT_EMAILS_FROM:ALERT_EMAILS_FROM}")
    private String from;

    // Replace recipient@example.com with a "To" address. If your account
    // is still in the sandbox, this address must be verified.
    @Value("${ALERT_EMAILS_TO:ALERT_EMAILS_TO}")
    private String to;

    @Value("${ALERT_EMAILS_SUBJECT:Error}")
    private String subject;


    @Value("${ALERT_EMAILS_ACCESS_ID:ACCESS_KEY}")
    private String accessId;


    @Value("${ALERT_EMAILS_ACCESS_SECRET:ACCESS_SECRET}")
    private String accessToken;

    /**
     * {@inheritDoc}.
     */
    @Override
    public void sendAlertEmail(final String message) {

        try {
            List<String> list = new ArrayList<>(Arrays.asList(to.split(",")));

            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials(accessId, accessToken)))
                            .withRegion(Regions.EU_WEST_1).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(list))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(message)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(subject + " " + getCurrentTimeStamp())))
                    .withSource(from);
            client.sendEmail(request);
            log.info("Error Email Sent");
        } catch (Exception ex) {
            log.severe(String.format("The email was not sent. Error message: %s", ex.getMessage()));
        }
    }

    /**
     * Get the current timestamp on the api.
     *
     * @return a string representation of the date time stamp
     */
    private String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
