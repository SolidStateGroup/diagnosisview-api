package com.solidstategroup.diagnosisview.service.impl;


import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.HostedPage.State;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.Status;
import com.chargebee.models.enums.AutoCollection;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.GoogleReceipt;
import com.solidstategroup.diagnosisview.model.PaymentDetails;
import com.solidstategroup.diagnosisview.model.SubscriptionData;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.enums.PaymentType;
import com.solidstategroup.diagnosisview.model.enums.SubscriptionType;
import com.solidstategroup.diagnosisview.payloads.ChargebeeSubscribePayload;
import com.solidstategroup.diagnosisview.results.ChargebeeHostedPageResult;
import com.solidstategroup.diagnosisview.service.SubscriptionService;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


/**
 * {@inheritDoc}.
 */
@Slf4j
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

  private final UserService userService;
  private final AppleReceiptValidation appleReceiptValidation;
  private final String chargebeeSite;
  private final String chargebeeApiKey;

  @Value("${IOS_SANDBOX:true}")
  private boolean isIosSandbox;

  @Value("${ANDROID_APPLICATION_NAME:NONE}")
  private String androidApplicationName;

  public SubscriptionServiceImpl(UserService userService,
      AppleReceiptValidation appleReceiptValidation,
      @Value("${chargebee.site}") String chargebeeSite,
      @Value("${chargebee.api.key}") String chargebeeApiKey) {
    this.userService = userService;
    this.appleReceiptValidation = appleReceiptValidation;
    this.chargebeeSite = chargebeeSite;
    this.chargebeeApiKey = chargebeeApiKey;
  }

  @Override
  public void checkSubscriptions() throws Exception {
    //Get all the users that are expiring soon
    userService.getExpiringUsers().forEach(user -> {
      //Get the latest payment data
      if (user.getPaymentData().size() > 0) {
        PaymentDetails payment = user.getPaymentData().get(user.getPaymentData().size() - 1);

        //If its android, run it against the verify android
        if ((payment.getPaymentType() != null && payment.getPaymentType().equals(PaymentType.ANDROID))) {
          try {
            verifyAndroidPurchase(user, payment.getGoogleReceipt());
          } catch (IOException e) {
            e.printStackTrace();
          } catch (GeneralSecurityException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User verifyAppleReceiptData(User user, String receipt) throws Exception {
    User savedUser = userService.getUser(user.getUsername());
    //validate the receipt using the sandbox (or use false for production)
    JsonObject responseJson = appleReceiptValidation.validateReceipt(receipt, isIosSandbox);
    //prints response
    log.info(responseJson.toString());

    PaymentDetails details = new PaymentDetails(responseJson.toString(), null, PaymentType.IOS);
    List<PaymentDetails> payments = savedUser.getPaymentData();
    payments.add(details);
    savedUser.setPaymentData(payments);
    Date expiryDate;

    // If the application is the test application, then add 1 hour to the expiry time,
    // otherwise, assume it is the production application and allow 1  year as the expiry time
    if (responseJson.get("receipt_type").toString().toLowerCase().contains("sandbox")) {
      expiryDate = new Date(Long.parseLong(new Gson().fromJson(details.getResponse(), Map.class)
          .get("receipt_creation_date_ms").toString()));
      DateTime plusOneHour = new DateTime(expiryDate).plusHours(1);
      expiryDate = plusOneHour.toDate();
    } else {
      expiryDate = new Date(Long.parseLong(new Gson().fromJson(details.getResponse(), Map.class)
          .get("receipt_creation_date_ms").toString()));
      DateTime plusOneHour = new DateTime(expiryDate).plusYears(1);
      expiryDate = plusOneHour.toDate();
    }

    //Hard coded for ios users
    savedUser.setAutoRenewing(false);
    savedUser.setExpiryDate(expiryDate);
    savedUser.setCurrentSubscription(SubscriptionType.IOS);
    savedUser.setActiveSubscription(true);
    userService.saveUser(savedUser);

    return savedUser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User verifyAndroidToken(User user, String receipt) throws Exception {
    User savedUser = userService.getUser(user.getUsername());
    log.info("verifyAndroidToken() for user receipt {}", receipt);
    Map<String, String> receiptMap = new Gson().fromJson(receipt, Map.class);
    Map<String, String> data = new Gson().fromJson(receiptMap.get("data"), Map.class);
    return verifyAndroidPurchase(savedUser,
        new GoogleReceipt(data.get("packageName"), data.get("productId"),
            data.get("purchaseToken")));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String verifyAndroidToken(String receipt) throws Exception {
    log.info("verifyAndroidToken() receipt {}", receipt);

    Map<String, String> receiptMap = new Gson().fromJson(receipt, Map.class);
    GoogleReceipt googleReceipt =
        new GoogleReceipt(receiptMap.get("packageName"), receiptMap.get("productId"),
            receiptMap.get("purchaseToken"));

    InputStream file = new ClassPathResource("google-play-key.json").getInputStream();

    GoogleCredential credential =
        GoogleCredential.fromStream(file)
            .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    AndroidPublisher pub = new AndroidPublisher.Builder
        (httpTransport, jsonFactory, credential)
        .setApplicationName(androidApplicationName)
        .build();

    final AndroidPublisher.Purchases.Subscriptions.Get get =
        pub.purchases()
            .subscriptions()
            .get(googleReceipt.getPackageName(),
                googleReceipt.getProductId(),
                googleReceipt.getToken());
    final SubscriptionPurchase purchase = get.execute();
    String purchaseString = purchase.toPrettyString();
    log.info("verifyAndroidToken() Found google purchase item {}", purchaseString);
    return purchaseString;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User verifyAndroidPurchase(User savedUser, GoogleReceipt googleReceipt) throws IOException,
      GeneralSecurityException {
    log.info("verifyAndroidPurchase() for user receipt {}", googleReceipt.toString());
    InputStream file = new ClassPathResource("google-play-key.json").getInputStream();

    GoogleCredential credential =
        GoogleCredential.fromStream(file)
            .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    AndroidPublisher pub = new AndroidPublisher.Builder
        (httpTransport, jsonFactory, credential)
        .setApplicationName(androidApplicationName)
        .build();

    final AndroidPublisher.Purchases.Subscriptions.Get get =
        pub.purchases()
            .subscriptions()
            .get(googleReceipt.getPackageName(),
                googleReceipt.getProductId(),
                googleReceipt.getToken());
    final SubscriptionPurchase purchase = get.execute();
    log.info("verifyAndroidPurchase() Found google purchase item {}", purchase.toPrettyString());

    List<PaymentDetails> payments = savedUser.getPaymentData();
    payments.add(new PaymentDetails(purchase.toString(), googleReceipt, PaymentType.ANDROID));

    savedUser.setActiveSubscription(true);
    savedUser.setCurrentSubscription(SubscriptionType.ANDROID);
    savedUser.setAutoRenewing(purchase.getAutoRenewing());
    savedUser.setExpiryDate(new Date(purchase.getExpiryTimeMillis()));
    userService.saveUser(savedUser);

    return savedUser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User validateChargebee(User user, ChargebeeSubscribePayload payload) {
    Environment.configure(chargebeeSite, chargebeeApiKey);
    Result result;
    try {
      result = HostedPage.retrieve(payload.getPageId()).request();
      HostedPage hostedPage = result.hostedPage();

      // hosted page successfully was submitted by the user
      if (hostedPage.state().equals(State.SUCCEEDED)) {
        Subscription subscription = hostedPage.content().subscription();

        user.setActiveSubscription(subscription.status().equals(Status.ACTIVE));
        user.setCurrentSubscription(SubscriptionType.CHARGEBEE);
        user.setSubscriptionData(new SubscriptionData(subscription.id()));

        if (hostedPage.content().customer() != null) {
          user.setAutoRenewing(
              hostedPage.content().customer().autoCollection().equals(AutoCollection.ON));
        }
        if (subscription.currentTermEnd() != null) {
          user.setExpiryDate(new Date(subscription.currentTermEnd().getTime()));
        }
      }

      user = userService.saveUser(user);
    } catch (Exception e) {
      log.error("There was a problem validating chargebee hosted page", e);
      throw new BadRequestException("Failed to validate chargebee.");
    }

    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ChargebeeHostedPageResult getChargebeeHostedPage(final User user) {

    if (!user.getCurrentSubscription().equals(SubscriptionType.CHARGEBEE)) {
      throw new BadRequestException("Current user subscription is not Chargebee.");
    }

    if (user.getSubscriptionData() == null) {
      throw new BadRequestException("Missing subscription data for account.");
    }

    try {
      Environment.configure(chargebeeSite, chargebeeApiKey);
      Result result = HostedPage.checkoutExistingForItems()
          .subscriptionId(user.getSubscriptionData().getSubscriptionId()).request();
      HostedPage hostedPage = result.hostedPage();

      return ChargebeeHostedPageResult.builder().url(hostedPage.url()).build();
    } catch (Exception e) {
      log.error("Failed to get chargebee hosted page", e);
      throw new BadRequestException("Failed to get chargebee hosted page.");
    }
  }
}
