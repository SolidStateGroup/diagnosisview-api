package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.PasswordResetDto;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.payloads.ForgotPasswordPayload;
import com.solidstategroup.diagnosisview.payloads.RegisterPayload;
import com.solidstategroup.diagnosisview.results.FavouriteResult;
import com.solidstategroup.diagnosisview.results.HistoryResult;
import java.util.List;

/**
 * Interface to interact with dashboard users.
 */
public interface UserService {

  /**
   * Add multiple favourites items to a user.
   *
   * @param user           the user to update
   * @param savedUserCodes the codes to add
   * @return User the updated user
   * @throws Exception
   */
  User addMultipleFavouritesToUser(final User user, final List<SavedUserCode> savedUserCodes)
      throws Exception;

  /**
   * Add a new favourite to a user.
   *
   * @param user          the user to update
   * @param savedUserCode the code to add
   * @return User the updated user
   * @throws Exception
   */
  User addFavouriteToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

  /**
   * Add multiple history items to a user.
   *
   * @param user           the user to update
   * @param savedUserCodes the codes to add
   * @return User the updated user
   * @throws Exception
   */
  User addMultipleHistoryToUser(final User user, final List<SavedUserCode> savedUserCodes)
      throws Exception;

  /**
   * Add a history item to a user.
   *
   * @param user          the user to update
   * @param savedUserCode the code to add
   * @return User the updated user
   * @throws Exception
   */
  User addHistoryToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

  /**
   * Register new User in the system.
   *
   * @param payload the user to create or update
   * @return User the created or updated user
   * @throws Exception thrown when cannot update user
   */
  User registerUser(RegisterPayload payload) throws Exception;

  /**
   * Create or update a user.
   *
   * @param user    the user to create or update
   * @param isAdmin flag to state whether the user updating the account is an admin.
   * @return User the created or updated user
   * @throws Exception thrown when cannot update user
   */
  User createOrUpdateUser(final User user, final boolean isAdmin) throws Exception;

  User saveUser(User user);

  /**
   * Delete user.
   *
   * @param user the user to create or update
   * @return the created or updated user
   * @throws Exception thrown when cannot update user
   */
  User deleteUser(final User user) throws Exception;

  /**
   * Remove a favourite from a user.
   *
   * @param user          the user to remove
   * @param savedUserCode the code to remove
   * @return User the updated user
   * @throws Exception
   */
  User deleteFavouriteToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

  /**
   * Remove a history item from a user.
   *
   * @param user          the user to update
   * @param savedUserCode the code to remove
   * @return User the updated user
   * @throws Exception
   */
  User deleteHistoryToUser(final User user, final SavedUserCode savedUserCode) throws Exception;

  /**
   * Get a list of favourite links for user.
   *
   * <p>List is filtered based on active subscription and hidden/excluded codes.
   *
   * @param user a user to get favourites for
   * @return a list of FavouriteResult
   * @throws ResourceNotFoundException
   */
  List<FavouriteResult> getFavouriteList(final User user) throws ResourceNotFoundException;

  /**
   * Returns a list of stored History codes items for user.
   *
   * <p>This method will also check if any of the history codes have been removed or hidden and
   * excludes them from returned list.
   *
   * @param user a user to get history for
   * @return a list of saved user history codes
   * @throws ResourceNotFoundException
   */
  List<HistoryResult> getHistoryList(final User user) throws ResourceNotFoundException;

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
   * Get expiring users
   *
   * @return the users expiring soon
   * @throws Exception thrown when cannot update user
   */
  List<User> getExpiringUsers() throws Exception;

  /**
   * Get users that are expiring in the next
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
   * Send the code to allow a user to reset their password
   *
   * @param payload - User details to reset password for
   * @throws Exception
   */
  void sendResetPassword(ForgotPasswordPayload payload) throws Exception;

  /**
   * Reset the users password using the reset code they enter
   *
   * @param passwordResetDto The required params to reset a password
   */
  void resetPassword(final PasswordResetDto passwordResetDto) throws Exception;
}
