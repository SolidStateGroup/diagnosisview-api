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
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.service.EmailService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * {@inheritDoc}.
 */
@Log
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${EMAILS_FROM:EMAILS_FROM}")
    private String from;

    @Value("${FEEDBACK_EMAIL:FEEDBACK_EMAIL}")
    private String feedbackEmail;


    @Value("${ALERT_EMAILS_ACCESS_ID:ACCESS_KEY}")
    private String accessId;


    @Value("${ALERT_EMAILS_ACCESS_SECRET:ACCESS_SECRET}")
    private String accessToken;


    private final String resetPasswordSubject = "DiagnosisView Reset Password";
    private final String feedbackSubject = "DiagnosisView Feedback";

    /**
     * {@inheritDoc}.
     */
    @Override
    public void sendForgottenPasswordEmail(final User user, final String resetCode) throws IOException {

        try {
            String html = generateResetPasswordEmail(user, resetCode);
            AmazonSimpleEmailService client;

            if (accessId != null && accessToken != null) {
                client = AmazonSimpleEmailServiceClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessId, accessToken)))
                        .withRegion(Regions.EU_WEST_1).build();
            } else {
                client = AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(Regions.EU_WEST_1).build();
            }

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(user.getEmailAddress()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content().withCharset("UTF-8").withData(html)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(resetPasswordSubject)))
                    .withSource(from);
            client.sendEmail(request);
            log.info("Error Email Sent");
        } catch (Exception ex) {
            log.severe(String.format("The email was not sent. Error message: %s", ex.getMessage()));
        }
    }

    @Override
    public void sendFeedback(final User user, final String message) {

        try {
            String html = generateFeedbackEmail(user, message);

            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials(accessId, accessToken)))
                            .withRegion(Regions.EU_WEST_1).build();

            //Prepare the send email request.
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(user.getEmailAddress()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content().withCharset("UTF-8").withData(html)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(feedbackSubject)))
                    .withSource(from);
            client.sendEmail(request);
            log.info("Error Email Sent");
        } catch (Exception ex) {
            log.severe(String.format("The email was not sent. Error message: %s", ex.getMessage()));
        }
    }


    private String generateFeedbackEmail(final User user, final String body) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("feedback-email.mustache");

        HashMap<String, String> content = new HashMap<>();
        content.put("body", body);

        if (user != null) {
            content.put("username", "Sent from user: " + user.getUsername());
        }

        StringWriter writer = new StringWriter();
        m.execute(writer, content).flush();
        return writer.toString();
    }

    private String generateResetPasswordEmail(final User user, final String resetCode) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("reset-password.mustache");

        HashMap<String, String> content = new HashMap<>();
        content.put("name", user.getFirstName());
        content.put("code", resetCode);

        StringWriter writer = new StringWriter();
        m.execute(writer, content).flush();
        return writer.toString();
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
