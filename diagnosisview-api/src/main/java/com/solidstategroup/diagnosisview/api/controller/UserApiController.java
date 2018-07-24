package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@Api(value = "/api/user", description = "Manage Users")
@RequestMapping("/api/user")
@Log
public class UserApiController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService     UserService manages the dashboard users
     */
    @Autowired
    public UserApiController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a user.
     * @param user User user to create
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ApiOperation(value = "Create User",
            notes = "Create a user, pass the password in which will then be encrypted",
            response = User.class)
    public User createUser(@RequestBody final User user) throws Exception {
        return userService.createOrUpdateUser(user);
    }

    /**
     * Update a user.
     * @param user User user to update
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    @ApiOperation(value = "Update User",
            notes = "Update a user, pass the password in which will then be encrypted",
            response = User.class)
    public User updateUser(@RequestBody final User user) throws Exception {
        return userService.createOrUpdateUser(user);
    }


    /**
     * Update a user.
     * @param user User user to update
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete User - TEST PURPOSES ONLY",
            notes = "Pass the user in with an ID to be deleted")
    public void deleteUser(@RequestBody final User user) throws Exception {
         userService.deleteUser(user);
    }

    /**
     * Get all users.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ApiOperation(value = "Get All Users",
            notes = "Admin User endpoint to get all users within the DiagnosisView",
            response = User.class)
    public List<User> getAllUsers() throws Exception {
        return userService.getAllUsers();
    }


    /**
     * Save a favourite for the user.
     *
     * @return User the updated users
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/user/favourites", method = RequestMethod.PUT)
    @ApiOperation(value = "Add a code to favourites",
            notes = "Adds a code to user favourites",
            response = User.class)
    public User saveFavourite(@RequestBody final Code favourite) throws Exception {
        return null;
    }

    /**
     * Save a favourite for the user.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/user/history", method = RequestMethod.PUT)
    @ApiOperation(value = "Save user history",
            notes = "Add a history item to users history",
            response = User.class)
    public User saveHistory(@RequestBody final String history) throws Exception {
        return null;
    }

}
