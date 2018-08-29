package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.model.PaymentDetails;
import com.solidstategroup.diagnosisview.model.enums.PaymentType;
import com.solidstategroup.diagnosisview.service.SubscriptionService;
import com.solidstategroup.diagnosisview.service.UserService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 * {@inheritDoc}.
 */
@Log
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    UserService userService;

    @Override
    public void checkSubscriptions() throws Exception {
        //Get all the users that are expiring soon
        userService.getExpiringUsers().stream().forEach(user -> {
            //Get the latest payment data
            if (user.getPaymentData().size() > 0) {
                PaymentDetails payment = user.getPaymentData().get(user.getPaymentData().size() - 1);

                //If its android, run it against the verify android
                if ((payment.getPaymentType() != null && payment.getPaymentType().equals(PaymentType.ANDROID))) {
                    try {
                        userService.verifyAndroidPurchase(user, payment.getGoogleReceipt());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }
}
