package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;

import java.util.List;

/**
 * Interface to interact with dashboard users.
 */
public interface UserService {

    /**
     * Add a new favourite to a user.
     *
     * @param user the user to update
     * @param savedUserCode the code to add
     * @return User the updated user
     * @throws Exception
     */
    User addFavouriteToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

    /**
     * Add a history item to a user.
     *
     * @param user the user to update
     * @param savedUserCode the code to add
     * @return User the updated user
     * @throws Exception
     */
    User addHistoryToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

    /**
     * Create or update a dashboard user.
     *
     * @param user the user to create or update
     * @return User the created or updated user
     * @throws Exception thrown when cannot update user
     */
    User createOrUpdateUser(final User user) throws Exception;

    /**
     * Delete user.
     *
     * @param user the user to create or update
     * @return the created or updated user
     * @throws Exception thrown when cannot update user
     */
    void deleteUser(final User user) throws Exception;

    /**
     * Remove a favourite from a user.
     *
     * @param user the user to remove
     * @param savedUserCode the code to remove
     * @return User the updated user
     * @throws Exception
     */
    User deleteFavouriteToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

    /**
     * Remove a history item from a user.
     *
     * @param user the user to update
     * @param savedUserCode the code to remove
     * @return User the updated user
     * @throws Exception
     */
    User deleteHistoryToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

    /**
     * Dashboard user login.
     *
     * @param username - entered username
     * @param password - entered password
     * @return the logged in user
     * @throws Exception thrown when cannot update user
     */
    User login(final String username, final String password) throws Exception;

    /**
     * Get a user by username.
     *
     * @param username supplied username
     * @return the found user
     * @throws Exception thrown when cannot update user
     */
    User getUser(final String username) throws Exception;

    /**
     * Get a user by token.
     *
     * @param token user token
     * @return the found user
     * @throws Exception thrown when cannot update user
     */
    User getUserByToken(final String token) throws Exception;

    /**
     * Get all dashboard users within the db.
     *
     * @return List all users within the db
     * @throws Exception thrown when cannot get users
     */
    List<User> getAllUsers() throws Exception;

    /**
     * Validates the Apple receipt against the api
     *
     * @param user - the user to update
     * @param receipt - the base64 encoded string
     * @return User the updated user
     */
    User verifyAppleReceiptData(User user, String receipt) throws Exception;

    /**
     * Validates the Android receipt against the api
     *
     * @param user - the user to update
     * @param receipt - the base64 encoded string
     * @return User the updated user
     */
    User verifyAndroidToken(User user, String receipt) throws Exception;

}
