package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.FeedbackDto;
import com.solidstategroup.diagnosisview.model.PasswordResetDto;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.service.EmailService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@Api(value = "/api/user", description = "Manage Users")
@RequestMapping("/api/user")
@Log
public class UserApiController extends BaseController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;
    private EmailService emailService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService UserService manages the dashboard users
     */
    @Autowired
    public UserApiController(final UserService userService,
                             final EmailService emailService) {
        super();
        this.userService = userService;
        this.emailService = emailService;
    }


    /**
     * Update a user.
     *
     * @param user User user to update
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/", method = RequestMethod.PUT)
    @ApiOperation(value = "Update User",
            notes = "Update a user, pass the password in which will then be encrypted",
            response = User.class)
    public User updateUser(@RequestBody final User user,
                           final HttpServletRequest request) throws Exception {

        User requestUser = checkIsAuthenticated(request);

        user.setId(requestUser.getId());
        user.setUsername(requestUser.getUsername());

        return userService.createOrUpdateUser(user, false);
    }


    /**
     * Send feedback to DV team
     * @param feedbackDto FeedbackDto
     * @throws Exception
     */
    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
    @ApiOperation(value = "Feedback",
            notes = "Sends feedback to DV team")
    public void sendDVFeedback(@RequestBody final FeedbackDto feedbackDto,
                               final HttpServletRequest request) throws Exception {
        User user = getUserFromRequest(request);
        emailService.sendFeedback(user, feedbackDto.getBody());
    }

    /**
     * Reset a users password using the passed in reset code
     *
     * @param passwordResetDto - the required fields to reset a password
     * @throws Exception
     */
    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    @ApiOperation(value = "Reset password",
            notes = "Reset the password of a user with the given reset code")
    public void resetPassword(@RequestBody final PasswordResetDto passwordResetDto) throws Exception {
        userService.resetPassword(passwordResetDto);

    }

    /**
     * Send a forgotten password email to a user
     *
     * @param user the user to sent the request for
     * @throws Exception
     */
    @RequestMapping(value = "/forgotten-password", method = RequestMethod.POST)
    @ApiOperation(value = "Forgotten password",
            notes = "Sends a reset password email to a user")
    public void forgottenPassword(@RequestBody final User user) throws Exception {
        userService.sendResetPassword(userService.getUser(user.getUsername()));
    }

    /**
     * Save a favourite for the user.
     *
     * @return User the updated users
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/favourites", method = RequestMethod.PUT)
    @ApiOperation(value = "Add a code to favourites",
            notes = "Adds a code to user favourites",
            response = User.class)
    public User saveFavourite(@RequestBody final SavedUserCode favourite,
                              final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = checkIsAuthenticated(request);

        return userService.addFavouriteToUser(user, favourite);
    }


    /**
     * Sync multiple history items for a user.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/sync/history", method = RequestMethod.PUT)
    @ApiOperation(value = "Save user history",
            notes = "Add a history item to users history",
            response = User.class)
    public User syncHistory(@RequestBody final List<SavedUserCode> historyList,
                            final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = checkIsAuthenticated(request);
        return userService.addMultipleHistoryToUser(user, historyList);
    }


    /**
     * Sync multiple favourites for a user.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/sync/favourites", method = RequestMethod.PUT)
    @ApiOperation(value = "Save user history",
            notes = "Add a history item to users history",
            response = User.class)
    public User syncFavourites(@RequestBody final List<SavedUserCode> favouriteList,
                               final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = checkIsAuthenticated(request);
        return userService.addMultipleFavouritesToUser(user, favouriteList);
    }


    /**
     * Save a history item for the user.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/history", method = RequestMethod.PUT)
    @ApiOperation(value = "Save user history",
            notes = "Add a history item to users history",
            response = User.class)
    public User saveHistory(@RequestBody final SavedUserCode history,
                            final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = checkIsAuthenticated(request);

        return userService.addHistoryToUser(user, history);
    }


    /**
     * Delete a favourite for the user.
     *
     * @return User the updated users
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/favourites", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a code from favourites",
            notes = "Deletes a code from user favourites",
            response = User.class)
    public User deleteFavourite(@RequestBody final SavedUserCode favourite,
                                final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = checkIsAuthenticated(request);

        return userService.deleteFavouriteToUser(user, favourite);
    }

    /**
     * Delete a history item for the user.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/history", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete user history item",
            notes = "Remove a history item to users history",
            response = User.class)
    public User deleteHistoryItem(@RequestBody final SavedUserCode history,
                                  final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = this.getUserFromRequest(request);
        if (user == null) {
            throw new Exception("You are not authenticated, please login to save favourites");
        }
        return userService.deleteHistoryToUser(user, history);
    }
}
