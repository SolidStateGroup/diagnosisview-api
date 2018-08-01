package com.solidstategroup.diagnosisview.service.impl;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.solidstategroup.diagnosisview.model.PaymentDetails;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.Utils;
import com.solidstategroup.diagnosisview.repository.UserRepository;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation;
import lombok.extern.java.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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

    @Value("${ANDROID_SERVICE_ACCOUNT_JSON:NONE}")
    private String androidServiceAccountJSON;

    @Value("${ANDROID_SERVICE_ACCOUNT:NONE}")
    private String androidServiceAccount;

    @Value("${ANDROID_APPLICATION_NAME:NONE}")
    private String androidApplicationName;

    /**
     * Constructor for the dashboard user service.
     *
     * @param userRepository the repo to autowire
     */
    @Autowired
    public UserServiceImpl(final UserRepository userRepository) {
        try {
            FileUtils.writeStringToFile(new File("google-play-key.json"), androidServiceAccountJSON);
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.userRepository = userRepository;
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
        userRepository.save(savedUser);
        return savedUser;
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
            userCodes = savedUser.getHistory();
        }

        userCodes.add(savedUserCode);

        savedUser.setHistory(userCodes);
        userRepository.save(savedUser);
        return savedUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User createOrUpdateUser(final User user) throws Exception {
        //this is a new user
        if (user.getId() == null) {
            if (userRepository.findOneByUsername(user.getUsername()) != null) {
                throw new IllegalStateException("This username already exists. Please try another one");
            }
            user.setDateCreated(new Date());
            user.setSalt(Utils.generateSalt());
            user.setPassword(DigestUtils.sha256Hex(user.getStoredPassword() +
                    user.getStoredSalt()));
            user.setToken(UUID.randomUUID().toString());

            return userRepository.save(user);

        } else {
            //Only certain fields can be updated, these are in this section.
            User savedUser = userRepository.findOneByUsername(user.getUsername());

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

            if (user.getStoredPassword() != null) {
                savedUser.setSalt(Utils.generateSalt());
                savedUser.setPassword(DigestUtils.sha256Hex(
                        String.format("%s%s", user.getStoredPassword(), user.getStoredSalt())));
            }

            return userRepository.save(savedUser);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(User user) throws Exception {
        User user1 = userRepository.findOneByUsername(user.getUsername());
        userRepository.delete(user1);
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


        if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
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
            savedCodesMap.remove(savedUserCode.getCode() + savedUserCode.getType(), savedUserCode);
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
        User user = userRepository.findOneByUsername(username);

        if (user == null) {
            throw new IllegalStateException("Please check your username and password.");
        }
        if (Utils.checkPassword(password, user.getStoredSalt(), user.getStoredPassword())) {
            //Update the user token on a sucessful login
            user.setToken(UUID.randomUUID().toString());
            userRepository.save(user);
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
        return userRepository.findOneByToken(token);
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
        try {
            User savedUser = this.getUser(user.getUsername());
            //validate the receipt using the sandbox (or use false for production)
            JsonObject responseJson = AppleReceiptValidation.validateReciept(receipt, true);
            //prints response
            log.info(responseJson.toString());

            PaymentDetails details = new PaymentDetails(responseJson.toString());
            List<PaymentDetails> payments = savedUser.getPaymentData();
            payments.add(details);
            savedUser.setPaymentData(payments);
            this.createOrUpdateUser(savedUser);

            return savedUser;
        } catch (AppleReceiptValidation.AppleReceiptValidationFailedException arvfEx) {
            arvfEx.printStackTrace();
            //do something to handle API error or invalid receipt...
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public User verifyAndroidToken(User user, String receipt) throws Exception {
        //Write the file to local storage
        URL filePath = Thread.currentThread().getContextClassLoader().getResource(
                "google-play-key.json");

        File file = new File(filePath.toURI());

        GoogleCredential credential =
                GoogleCredential.fromStream(
                        new FileInputStream(file))
                        .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        Map<String, String> receiptMap = new Gson().fromJson(receipt, Map.class);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        AndroidPublisher pub = new AndroidPublisher.Builder
                (httpTransport, jsonFactory, credential)
                .setApplicationName(androidApplicationName)
                .build();
        final AndroidPublisher.Purchases.Products.Get get =
                pub.purchases()
                        .products()
                        .get(receiptMap.get("packageName"),
                                receiptMap.get("productId"),
                                receiptMap.get("purchaseToken"));
        final ProductPurchase purchase = get.execute();
        log.info("Found google purchase item " + purchase.toPrettyString());

        return user;
    }

}
