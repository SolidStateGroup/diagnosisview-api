package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.service.UserService;
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
@RequestMapping("/api/admin")
@Log
public class AdminApiController extends BaseController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService UserService manages the dashboard users
     */
    @Autowired
    public AdminApiController(final UserService userService) {
        super();
        this.userService = userService;
    }

    /**
     * User login to system.
     *
     * @param user user to login
     * @return User the logged in user
     * @throws Exception thrown when user cannot be logged in
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public User login(@RequestBody final User user) throws Exception {
        User loggedInUser = userService.login(user.getUsername(), user.getStoredPassword());
        //Temp disabled whilst TS is on holiday
//        if (loggedInUser == null || loggedInUser.getRoleType().equals(RoleType.USER)) {
//            throw new IllegalStateException("Please check your username and password.");
//        }
        log.info("Logging in Admin - " + loggedInUser.getUsername());
        return loggedInUser;
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
    public List<User> getAllUsers(HttpServletRequest request) throws Exception {
        //TODO Add this back in
        //isAdminUser(request);

        return userService.getAllUsers();
    }


    /**
     * Update a user.
     *
     * @param user User user to update
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete User - TEST PURPOSES ONLY",
            notes = "Pass the user in with an ID to be deleted")
    public User deleteUser(@RequestBody final User user,
                           HttpServletRequest request) throws Exception {
        //TODO Add this back in
        //isAdminUser(request);

        //Soft delete, making user as deleted
        return userService.deleteUser(user);
    }


    /**
     * Create a user.
     *
     * @param user User user to create
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ApiOperation(value = "Create User",
            notes = "Create a user, pass the password in which will then be encrypted",
            response = User.class)
    public User createUser(@RequestBody final User user) throws Exception {
        return userService.createOrUpdateUser(user);
    }


}
