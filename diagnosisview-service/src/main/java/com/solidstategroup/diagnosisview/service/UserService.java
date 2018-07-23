package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.User;

import java.util.List;

/**
 * Interface to interact with dashboard users.
 */
public interface UserService {

    /**
     * Create or update a dashboard user.
     *
     * @param user the user to create or update
     * @return the created or updated user
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

}
