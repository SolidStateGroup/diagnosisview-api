package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.model.User;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@RequestMapping("/api")
@Log
public class ApiController extends BaseController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService UserService manages the dashboard users
     */
    @Autowired
    public ApiController(final UserService userService) {
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
        return userService.login(user.getUsername(), user.getStoredPassword());
    }

    /**
     * Get the current user that is logged into the api.
     *
     * @return User the logged in user
     * @throws Exception thrown when user cannot be logged in
     */
    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public User getAccount(final HttpServletRequest request) throws Exception {
        return this.getUserFromRequest(request);
    }


    /**
     * User wants to register
     *
     * @param user user to login
     * @return User user to reset
     * @throws Exception thrown when user cannot be logged in
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public User register(@RequestBody final User user) throws Exception {
        return userService.createOrUpdateUser(user);
    }
}
