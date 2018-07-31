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
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.Utils;
import com.solidstategroup.diagnosisview.repository.UserRepository;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation;
import lombok.extern.java.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Value("${ANDROID_SERVICE_ACCOUNT:NONE}")
    private String androidServiceAccount;

    @Value("${ANDROID_APPLICATION_NAME:NONE}")
    private String androidApplicationName;

    @Value("${ANDROID_PACKAGE_NAME:NONE}")
    private String androidPackageName;

    /**
     * Constructor for the dashboard user service.
     *
     * @param userRepository the repo to autowire
     */
    @Autowired
    public UserServiceImpl(final UserRepository userRepository) {
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
            Arrays.stream(savedUser.getFavourites()).forEach(savedCode -> {
                savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode);
            });
        }

        if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
            savedCodesMap.put(savedUserCode.getCode() + savedUserCode.getType(), savedUserCode);
        }


        savedUser.setFavourites(savedCodesMap.values().toArray(new SavedUserCode[savedCodesMap.size()]));
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
            userCodes = Arrays.asList(savedUser.getHistory());
        }

        userCodes.add(savedUserCode);

        savedUser.setHistory(userCodes.toArray(new SavedUserCode[userCodes.size()]));
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
        } else {
            user.setSalt(Utils.generateSalt());
            user.setPassword(DigestUtils.sha256Hex(
                    String.format("%s%s", user.getStoredPassword(), user.getStoredSalt())));
        }
        if (user.getLogoData() != null) {
            user.setProfileImage(Base64.decode(new String(user.getLogoData()).getBytes("UTF-8")));
        }

        return userRepository.save(user);
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
        Arrays.stream(savedUser.getFavourites()).forEach(savedCode -> {
            savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode);
        });


        if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
            savedCodesMap.remove(savedUserCode.getCode() + savedUserCode.getType(), savedUserCode);
        }


        savedUser.setFavourites(savedCodesMap.values().toArray(new SavedUserCode[savedCodesMap.size()]));
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
        Arrays.stream(savedUser.getHistory()).forEach(savedCode -> {
            savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode);
        });


        if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
            savedCodesMap.remove(savedUserCode.getCode() + savedUserCode.getType(), savedUserCode);
        }


        savedUser.setHistory(savedCodesMap.values().toArray(new SavedUserCode[savedCodesMap.size()]));
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
     * Get the receipt data for an apple receipt
     *
     * @return the map with the data
     */
    public String getAppleReceiptData(String receipt) {
        try {
            //validate the receipt using the sandbox (or use false for production)
            JsonObject responseJson = AppleReceiptValidation.validateReciept(receipt, true);
            //prints response
            log.info(responseJson.getAsString());

            return responseJson.getAsString();
        } catch (AppleReceiptValidation.AppleReceiptValidationFailedException arvfEx) {
            arvfEx.printStackTrace();
            //do something to handle API error or invalid receipt...
        }

        return null;
    }


    public String verifyAndroidToken(String receipt) throws Exception {

        Map<String, String> receiptMap = new Gson().fromJson(receipt, Map.class);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        Set<String> scopes = Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER);
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId("")
                .setServiceAccountScopes(scopes)
                .setServiceAccountPrivateKeyFromP12File(
                        new File("")).build();


        AndroidPublisher pub = new AndroidPublisher.Builder
                (httpTransport, jsonFactory, credential)
                .setApplicationName(androidApplicationName)
                .build();
        final AndroidPublisher.Purchases.Products.Get get =
                pub.purchases()
                        .products()
                        .get(androidPackageName, receiptMap.get("productId"), receiptMap.get("userPurchaseToken"));
        final ProductPurchase purchase = get.execute();
        log.info("Found google purchase item " + purchase.toPrettyString());

        return purchase.toPrettyString();
    }

}
