package com.solidstategroup.diagnosisview.service.impl;


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
import com.solidstategroup.diagnosisview.model.PasswordResetDto;
import com.solidstategroup.diagnosisview.model.PaymentDetails;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.Utils;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.model.enums.PaymentType;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.repository.UserRepository;
import com.solidstategroup.diagnosisview.service.EmailService;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/**
 * {@inheritDoc}.
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AppleReceiptValidation appleReceiptValidation;
  private final EmailService emailService;
  private final InstitutionService institutionService;

  @Value("${IOS_SANDBOX:true}")
  private boolean isIosSandbox;

  @Value("${ANDROID_APPLICATION_NAME:NONE}")
  private String androidApplicationName;

  /**
   * Constructor for the dashboard user service.
   *
   * @param userRepository the repo to autowire
   */
  @Autowired
  public UserServiceImpl(final UserRepository userRepository,
      final AppleReceiptValidation appleReceiptValidation,
      final EmailService emailService,
      final InstitutionService institutionService) {
    this.userRepository = userRepository;
    this.appleReceiptValidation = appleReceiptValidation;
    this.emailService = emailService;
    this.institutionService = institutionService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addMultipleFavouritesToUser(User user, List<SavedUserCode> savedUserCodes)
      throws Exception {
    User savedUser = this.getUser(user.getUsername());
    HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
    if (savedUser.getFavourites() != null) {
      savedUser.getFavourites().forEach(savedCode -> savedCodesMap.put(
          savedCode.getLinkId() + savedCode.getCode() + savedCode.getType(), savedCode));
    }

    for (SavedUserCode savedUserCode : savedUserCodes) {
      validateFavourite(savedUserCode);
      if (!savedCodesMap
          .containsKey(
              savedUserCode.getLinkId() + savedUserCode.getCode() + savedUserCode.getType())) {
        savedCodesMap
            .put(savedUserCode.getLinkId() + savedUserCode.getCode() +
                savedUserCode.getType(), savedUserCode);
      }
    }

    savedUser.setFavourites(new ArrayList<>(savedCodesMap.values()));
    userRepository.save(savedUser);
    return savedUser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addFavouriteToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());

    validateFavourite(savedUserCode);

    HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
    if (savedUser.getFavourites() != null) {
      savedUser.getFavourites().forEach(savedCode -> savedCodesMap.put(
          savedCode.getLinkId() + savedCode.getCode() + savedCode.getType(),
          savedCode));
    }

    if (!savedCodesMap
        .containsKey(
            savedUserCode.getLinkId() + savedUserCode.getCode() + savedUserCode.getType())) {
      savedCodesMap
          .put(savedUserCode.getLinkId() + savedUserCode.getCode() + savedUserCode.getType(),
              savedUserCode);
    }

    savedUser.setFavourites(new ArrayList<>(savedCodesMap.values()));
    return userRepository.save(savedUser);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addMultipleHistoryToUser(User user, List<SavedUserCode> savedUserCodes)
      throws Exception {
    User savedUser = this.getUser(user.getUsername());
    List<SavedUserCode> userCodes = new ArrayList<>();

    if (savedUser.getHistory() != null) {
      savedUser.getHistory().forEach(history -> userCodes.add(history));
    }

    for (SavedUserCode savedUserCode : savedUserCodes) {
      if (savedUserCode.getDateAdded() == null) {
        savedUserCode.setDateAdded(new Date());
      }
      userCodes.add(savedUserCode);
    }

    savedUser.setHistory(new ArrayList(userCodes));
    return userRepository.save(savedUser);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addHistoryToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());
    if (savedUserCode.getDateAdded() == null) {
      savedUserCode.setDateAdded(new Date());
    }
    List<SavedUserCode> userCodes = new ArrayList<>();

    if (savedUser.getHistory() != null) {
      savedUser.getHistory().forEach(history -> userCodes.add(history));
    }

    userCodes.add(savedUserCode);

    savedUser.setHistory(new ArrayList(userCodes));
    return userRepository.save(savedUser);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User createOrUpdateUser(final User user, boolean isAdmin) throws Exception {
    //this is a new user
    if (user.getId() == null) {
      if (userRepository.findOneByUsername(user.getUsername()) != null) {
        throw new IllegalStateException(
            String.format("The username %s already exists. Please try another " +
                "one", user.getUsername()));
      }
      user.setUsername(user.getUsername());
      user.setEmailAddress(user.getEmailAddress());
      user.setDateCreated(new Date());
      user.setSalt(Utils.generateSalt());
      user.setPassword(DigestUtils.sha256Hex(user.getStoredPassword() +
          user.getStoredSalt()));
      user.setToken(UUID.randomUUID().toString());
      user.setRoleType(RoleType.USER);

      // check make sure we have correct selected institution
      if (!StringUtils.isEmpty(user.getInstitution())) {
        Institution institution = institutionService.getInstitution(user.getInstitution());
        user.setInstitution(institution.getCode());
      }

      return userRepository.save(user);

    } else {
      User savedUser;

      if (user.getId() != null) {
        savedUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new IllegalStateException("Could not find user"));
      } else {
        //Only certain fields can be updated, these are in this section.
        savedUser = userRepository.findOneByUsername(user.getUsername().toLowerCase());
      }

      if (user.getFirstName() != null) {
        savedUser.setFirstName(user.getFirstName());
      }

      if (user.getLastName() != null) {
        savedUser.setLastName(user.getLastName());
      }

      if (user.getOccupation() != null) {
        savedUser.setOccupation(user.getOccupation());
      }
      if (user.getInstitution() != null) {
        Institution institution = institutionService.getInstitution(user.getInstitution());
        savedUser.setInstitution(institution.getCode());
      }

      if (user.getEmailAddress() != null) {
        savedUser.setEmailAddress(user.getEmailAddress());
      }

      if (user.getExpiryDate() != null && isAdmin) {
        savedUser.setExpiryDate(user.getExpiryDate());
        if (user.getExpiryDate().after(new Date())) {
          savedUser.setActiveSubscription(true);
        }
      }

      if (user.getStoredPassword() != null) {

        if (!Utils.checkPassword(user.getOldPassword(), user.getStoredSalt(),
            user.getStoredPassword())) {
          throw new BadCredentialsException("Current password incorrect. Please try again.");
        }

        savedUser.setSalt(Utils.generateSalt());
        savedUser.setPassword(DigestUtils.sha256Hex(user.getStoredPassword() +
            savedUser.getStoredSalt()));
      }

      return userRepository.save(savedUser);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User deleteUser(User user) {
    User user1 = userRepository.findOneByUsername(user.getUsername());
    user1.setDeleted(true);
    userRepository.save(user1);

    return user1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User deleteFavouriteToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());
    savedUser.getFavourites().removeIf(f ->
        f.getLinkId().equals(savedUserCode.getLinkId())
            && f.getCode().equalsIgnoreCase(savedUserCode.getCode())
            && f.getType().equalsIgnoreCase(savedUserCode.getType()));

    userRepository.save(savedUser);
    return savedUser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User deleteHistoryToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());
    HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
    savedUser.getHistory().forEach(
        savedCode -> savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode));

    if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
      savedCodesMap.remove(savedUserCode.getCode() + savedUserCode.getType());
    }

    savedUser.setHistory(new ArrayList<>(savedCodesMap.values()));
    userRepository.save(savedUser);
    return savedUser;
  }


  @Override
  public List<SavedUserCode> getFavouriteList(final User user) {
    List<SavedUserCode> favourite = user.getFavourites();

    // if user not subscribed, return only last 20
    if (favourite.size() > 20 && !user.isActiveSubscription()) {
      return favourite.subList(favourite.size() - 20, favourite.size());
    } else {
      return favourite;
    }
  }

  @Override
  public List<SavedUserCode> getHistoryList(final User user) {
    List<SavedUserCode> history = user.getHistory();

    // if user not subscribed, return only last 20
    if (history.size() > 20 && !user.isActiveSubscription()) {
      return history.subList(history.size() - 20, history.size());
    } else {
      return history;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User login(final String username, final String password) throws Exception {

    User user = userRepository.findOneByUsername(username.toLowerCase());

    if (user == null) {
      throw new BadCredentialsException("Login failed - please check your username and password.");
    }
    if (Utils.checkPassword(password, user.getStoredSalt(), user.getStoredPassword())) {
      if (user.isDeleted()) {
        throw new BadCredentialsException("This account has been deleted. " +
            "Please contact support@diagnosisview.org.");
      }

      //Admin users will always have a subscription
      if (user.getRoleType().equals(RoleType.ADMIN)) {
        user.setActiveSubscription(true);
        userRepository.save(user);
      }
      //If the user is not auto-renewing, and the expiry date has past, set them to inactive
      else if (!user.isAutoRenewing() && (user.getExpiryDate() == null ||
          user.getExpiryDate().before(new Date()))) {
        user.setActiveSubscription(false);
        userRepository.save(user);
      }

      return user;

    } else {

      throw new BadCredentialsException("Login failed - please check your username and password");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User getUser(final String username) {
    return userRepository.findOneByUsername(username);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<User> getExpiringUsers() throws Exception {
    return userRepository
        .findByExpiryDateLessThanEqualAndActiveSubscription(new DateTime().plusWeeks(1).toDate(),
            true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User getUserByToken(final String token) throws Exception {
    User user = userRepository.findOneByToken(token);

    //If no user is found, return null
    if (user == null) {
      return null;
    }

    //Admin users will always have a subscription
    if (user.getRoleType().equals(RoleType.ADMIN)) {
      user.setActiveSubscription(true);
      userRepository.save(user);
    }
    //If the user is not auto-renewing, and the expiry date has past, set them to inactive
    else if (!user.isAutoRenewing() && (user.getExpiryDate() == null || user.getExpiryDate()
        .before(new Date()))) {
      user.setActiveSubscription(false);
      userRepository.save(user);
    }

    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<User> getAllUsers() throws Exception {
    return userRepository.findAll(new Sort(Sort.Direction.ASC, "dateCreated"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendResetPassword(User user) throws Exception {
    int length = 6;
    boolean useLetters = true;
    boolean useNumbers = true;
    String generatedString = RandomStringUtils.random(length, useLetters, useNumbers).toUpperCase();

    User existingUser = userRepository.findOneByUsername(user.getUsername());
    if (existingUser.getResetExpiryDate() == null || existingUser.getResetExpiryDate()
        .before(new Date())) {
      existingUser.setResetCode(generatedString);
      DateTime oneDayAdded = new DateTime().plusHours(1);
      existingUser.setResetExpiryDate(oneDayAdded.toDate());
      userRepository.save(existingUser);
    }

    emailService.sendForgottenPasswordEmail(user, existingUser.getResetCode());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User resetPassword(final PasswordResetDto resetDto) throws Exception {
    //Get the user from the db
    User user = this.getUser(resetDto.getUsername());

    //If the user doesnt exist, throw an error
    if (user == null) {
      throw new BadRequestException("We were unable to validate your request. " +
          "Please check your username and reset code.");
    }

    //Check the reset code hasnt expired
    if (user.getResetExpiryDate().before(new Date())) {
      throw new BadRequestException("Your request has expired. Please request a new reset code");
    }
    //Check the reset code is ok
    if (!user.getResetCode().equals(resetDto.getResetCode().toUpperCase())) {
      throw new BadRequestException("We were unable to validate your request. " +
          "Please check your username and reset code.");
    }

    //Update the password and salt
    user.setResetExpiryDate(null);
    user.setResetCode(null);
    userRepository.save(user);

    user.setPassword(resetDto.getNewPassword());
    return this.createOrUpdateUser(user, true);
  }


  /**
   * {@inheritDoc}
   */
  public User verifyAppleReceiptData(User user, String receipt) throws Exception {
    User savedUser = this.getUser(user.getUsername());
    //validate the receipt using the sandbox (or use false for production)
    JsonObject responseJson = appleReceiptValidation.validateReciept(receipt, isIosSandbox);
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
    savedUser.setActiveSubscription(true);
    userRepository.save(savedUser);

    return savedUser;
  }

  /**
   * {@inheritDoc}
   */
  public User verifyAndroidToken(User user, String receipt) throws Exception {
    User savedUser = this.getUser(user.getUsername());

    Map<String, String> receiptMap = new Gson().fromJson(receipt, Map.class);
    Map<String, String> data = new Gson().fromJson(receiptMap.get("data"), Map.class);
    return verifyAndroidPurchase(savedUser,
        new GoogleReceipt(data.get("packageName"), data.get("productId"),
            data.get("purchaseToken")));
  }

  /**
   * {@inheritDoc}
   */
  public String verifyAndroidToken(String receipt) throws Exception {

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
    log.info("Found google purchase item {}" + purchaseString);
    return purchaseString;
  }


  /**
   * {@inheritDoc}
   */
  public User verifyAndroidPurchase(User savedUser, GoogleReceipt googleReceipt) throws IOException,
      GeneralSecurityException {

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
    log.info("Found google purchase item " + purchase.toPrettyString());

    List<PaymentDetails> payments = savedUser.getPaymentData();
    payments.add(new PaymentDetails(purchase.toString(), googleReceipt, PaymentType.ANDROID));

    savedUser.setActiveSubscription(true);
    savedUser.setAutoRenewing(purchase.getAutoRenewing());
    savedUser.setExpiryDate(new Date(purchase.getExpiryTimeMillis()));
    userRepository.save(savedUser);

    return savedUser;
  }


  /**
   * Validate give given favourite object
   *
   * @param favourite a favourite to validate
   * @throws Exception when failed validation
   */
  private void validateFavourite(SavedUserCode favourite) throws Exception {

    if (favourite.getLinkId() == null) {
      log.error("Missing link id from favourite");
      throw new Exception("Missing link id for favourite");
    }

    if (StringUtils.isEmpty(favourite.getCode())) {
      log.error("Missing code from favourite");
      throw new Exception("Missing code for favourite");
    }

    if (StringUtils.isEmpty(favourite.getType())) {
      log.error("Missing type from favourite");
      throw new Exception("Missing type for Diagnosis Code");
    }
  }

}
