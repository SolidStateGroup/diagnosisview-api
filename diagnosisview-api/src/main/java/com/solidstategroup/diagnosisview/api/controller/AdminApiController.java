package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.service.UserService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@RequestMapping("/api/admin")
@Log
public class AdminApiController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService     UserService manages the dashboard users
     */
    @Autowired
    public AdminApiController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a user.
     * @param user User user to create
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users", method = RequestMethod.POST)
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
    public User updateUser(@RequestBody final User user) throws Exception {
        return userService.createOrUpdateUser(user);
    }


    /**
     * Update a user.
     * @param user User user to update
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users", method = RequestMethod.DELETE)
    public void deleteUser(@RequestBody final User user) throws Exception {
         userService.deleteUser(user);
    }

    /**
     * Get user by username.
     * @param username String username to lookup
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users/{username}", method = RequestMethod.GET)
    public User getUserByUsername(@PathVariable(value = "username") final String username) throws Exception {
        return userService.getUser(username);
    }

    /**
     * Get all users.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> getAllUsers() throws Exception {
        return userService.getAllUsers();
    }

}
