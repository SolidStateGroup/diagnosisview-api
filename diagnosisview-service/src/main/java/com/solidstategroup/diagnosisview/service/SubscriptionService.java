package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.GoogleReceipt;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.payloads.ChargebeeSubscribePayload;
import com.solidstategroup.diagnosisview.results.ChargebeeHostedPageResult;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Subscription service to check renewable subscriptions
 */
public interface SubscriptionService {

  /**
   * Validate user subscriptions that are ending in the next month
   */
  void checkSubscriptions() throws Exception;

  /**
   * Validates the Apple receipt against the api
   *
   * @param user    the user to update
   * @param receipt the base64 encoded string
   * @return User the updated user
   */
  User verifyAppleReceiptData(User user, String receipt) throws Exception;

  /**
   * Validates the Android receipt against the api
   *
   * @param user    the user to update
   * @param receipt the base64 encoded string
   * @return User the updated user
   */
  User verifyAndroidToken(User user, String receipt) throws Exception;

  /**
   * Validates the Android receipt against the api
   *
   * @param receipt the base64 encoded string
   * @return User the updated user
   */
  String verifyAndroidToken(String receipt) throws Exception;

  /**
   * Verify an Android purchase against the google play API
   *
   * @param savedUser     the saved user
   * @param googleReceipt the receipt object to verify
   * @return the updated user
   */
  User verifyAndroidPurchase(User savedUser, GoogleReceipt googleReceipt)
      throws IOException, GeneralSecurityException;

  /**
   * After client submitted hosted page validate the subscription.
   *
   * @param user    a user account to validate subscription for
   * @param payload a payload containing validation data
   * @return a User
   * @throws Exception
   */
  User validateChargebee(User user, ChargebeeSubscribePayload payload) throws Exception;

  ChargebeeHostedPageResult getChargebeeHostedPage(User user);

}
