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
import com.solidstategroup.diagnosisview.exceptions.NotAuthorisedException;
import com.solidstategroup.diagnosisview.model.PaymentDetails;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.Utils;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.repository.UserRepository;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation;
import lombok.extern.java.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * {@inheritDoc}.
 */
@Log
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Value("${APPLE_URL:https://sandbox.itunes.apple.com/verifyReceipt}")
    private String appleUrlString;

    @Value("${ANDROID_APPLICATION_NAME:NONE}")
    private String androidApplicationName;
    private AppleReceiptValidation appleReceiptValidation;

    /**
     * Constructor for the dashboard user service.
     *
     * @param userRepository the repo to autowire
     */
    @Autowired
    public UserServiceImpl(final UserRepository userRepository,
                           final AppleReceiptValidation appleReceiptValidation) {
        this.userRepository = userRepository;
        this.appleReceiptValidation = appleReceiptValidation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User addMultipleFavouritesToUser(User user, List<SavedUserCode> savedUserCodes) throws Exception {
        User savedUser = this.getUser(user.getUsername());
        HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
        if (savedUser.getFavourites() != null) {
            savedUser.getFavourites().stream().forEach(savedCode -> {
                savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode);
            });
        }

        savedUserCodes.stream().forEach(savedUserCode -> {
            if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
                savedCodesMap.put(savedUserCode.getCode() + savedUserCode.getType(), savedUserCode);
            }
        });

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
        HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
        if (savedUser.getFavourites() != null) {
            savedUser.getFavourites().stream().forEach(savedCode -> {
                savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode);
            });
        }

        if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
            savedCodesMap.put(savedUserCode.getCode() + savedUserCode.getType(), savedUserCode);
        }


        savedUser.setFavourites(new ArrayList<>(savedCodesMap.values()));
        return userRepository.save(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User addMultipleHistoryToUser(User user, List<SavedUserCode> savedUserCodes) throws Exception {
        User savedUser = this.getUser(user.getUsername());
        List<SavedUserCode> userCodes = new ArrayList<>();

        if (savedUser.getHistory() != null) {
            savedUser.getHistory().stream().forEach(history -> {
                userCodes.add(history);
            });
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
            savedUser.getHistory().stream().forEach(history -> {
                userCodes.add(history);
            });
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
                throw new IllegalStateException(String.format("The username %s already exists. Please try another " +
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

            return userRepository.save(user);

        } else {
            User savedUser = null;

            if (user.getId() != null) {
                savedUser = userRepository.findOne(user.getId());
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
                savedUser.setInstitution(user.getInstitution());
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
                //If the user isnt an admin, we need to ensure that the password matches the old one
                if (!isAdmin) {
                    //Check the old password
                    User userLogin = this.login(user.getUsername(), user.getOldPassword());

                    if (userLogin == null) {
                        throw new NotAuthorisedException("Your password does not appear to be correct. " +
                                "Please check and try again.");
                    }
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
    public User deleteUser(User user) throws Exception {
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
        HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
        savedUser.getFavourites().stream().forEach(savedCode -> {
            savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode);
        });


        if (savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
            savedCodesMap.remove(savedUserCode.getCode() + savedUserCode.getType(), savedUserCode);
        }


        savedUser.setFavourites(new ArrayList<>(savedCodesMap.values()));
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
        savedUser.getHistory().stream().forEach(savedCode -> {
            savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode);
        });


        if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
            savedCodesMap.remove(savedUserCode.getCode() + savedUserCode.getType());
        }


        savedUser.setHistory(new ArrayList<>(savedCodesMap.values()));
        userRepository.save(savedUser);
        return savedUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User login(final String username, final String password) throws Exception {
        User user = userRepository.findOneByUsername(username.toLowerCase());

        if (user == null) {
            throw new IllegalStateException("Please check your username and password.");
        }
        if (Utils.checkPassword(password, user.getStoredSalt(), user.getStoredPassword())) {
            if (user.isDeleted()) {
                throw new IllegalStateException("This account has been deleted. " +
                        "Please contact support@diagnosisview.org.");
            }
            //Update the user token on a sucessful login
            user.setToken(UUID.randomUUID().toString());

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
            throw new IllegalStateException("Please check your username and password");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser(final String username) throws Exception {
        return userRepository.findOneByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByToken(final String token) throws Exception {
        User user = userRepository.findOneByToken(token);

        if (user == null) {
            throw new NotAuthorisedException("You are not authenticated, please try logging in again.");
        }

        //Admin users will always have a subscription
        if (user.getRoleType().equals(RoleType.ADMIN)) {
            user.setActiveSubscription(true);
            userRepository.save(user);
        }
        //If the user is not auto-renewing, and the expiry date has past, set them to inactive
        else if (!user.isAutoRenewing() && (user.getExpiryDate() == null || user.getExpiryDate().before(new Date()))) {
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
    public User verifyAppleReceiptData(User user, String receipt) throws Exception {
        User savedUser = this.getUser(user.getUsername());
        //validate the receipt using the sandbox (or use false for production)
        JsonObject responseJson = appleReceiptValidation.validateReciept(receipt, true);
        //prints response
        log.info(responseJson.toString());

        PaymentDetails details = new PaymentDetails(responseJson.toString());
        List<PaymentDetails> payments = savedUser.getPaymentData();
        payments.add(details);
        savedUser.setPaymentData(payments);
        Date expiryDate = new Date(Long.parseLong(new Gson().fromJson(details.getResponse(), Map.class)
                .get("original_purchase_date_ms").toString()));

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

        InputStream file = new ClassPathResource("google-play-key.json").getInputStream();

        GoogleCredential credential =
                GoogleCredential.fromStream(file)
                        .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        Map<String, String> receiptMap = new Gson().fromJson(receipt, Map.class);
        Map<String, String> data = new Gson().fromJson(receiptMap.get("data"), Map.class);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        AndroidPublisher pub = new AndroidPublisher.Builder
                (httpTransport, jsonFactory, credential)
                .setApplicationName(androidApplicationName)
                .build();

        final AndroidPublisher.Purchases.Subscriptions.Get get =
                pub.purchases()
                        .subscriptions()
                        .get(data.get("packageName"),
                                data.get("productId"),
                                data.get("purchaseToken"));
        final SubscriptionPurchase purchase = get.execute();
        log.info("Found google purchase item " + purchase.toPrettyString());

        List<PaymentDetails> payments = savedUser.getPaymentData();
        payments.add(new PaymentDetails(purchase.toString()));
        savedUser.setActiveSubscription(true);
        Map<String, String> response = new Gson().fromJson(purchase.toString(), Map.class);

        savedUser.setAutoRenewing(Boolean.parseBoolean(response.get("autoRenewing").toString()));
        savedUser.setExpiryDate(new Date(Long.parseLong(response.get("expiryTimeMillis"))));
        userRepository.save(savedUser);

        return savedUser;
    }

}
