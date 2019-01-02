package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.exceptions.ImageIOException;
import com.solidstategroup.diagnosisview.exceptions.ImageNotFoundException;
import com.solidstategroup.diagnosisview.exceptions.NotAuthorisedException;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseController {

    UserService userService;

    protected BaseController(final UserService userService) {

        this.userService = userService;
    }

    protected  BaseController() {}

    /**
     * Get the user based on the user token.
     *
     * @param request
     * @return User the user or null
     */
    protected User getUserFromRequest(final HttpServletRequest request) throws Exception {
        return userService.getUserByToken(getToken(request));
    }

    /**
     * Check whether the user for the request is authenticated
     *
     * @param request the request to check
     * @return User the user is the request is authenticated
     * @throws Exception - when the user is not logged in
     */
    public User checkIsAuthenticated(final HttpServletRequest request) throws Exception {
        User requestUser = userService.getUserByToken(getToken(request));
        if (requestUser == null) {
            throw new NotAuthorisedException("You are not authenticated, please try logging in again.");
        }
        return requestUser;
    }

    /**
     * Check whether the user for the request is an admin.
     *
     * @param request the request to check
     * @return Boolean if the user is an admin
     * @throws Exception
     */
    public void isAdminUser(final HttpServletRequest request) throws Exception {
        User user = userService.getUserByToken(getToken(request));

        if (user == null) {
            throw new NotAuthorisedException("You are not authenticated, please try logging in again.");
        }
        //Throw an error if the user is not an admin
        if (!user.getRoleType().equals(RoleType.ADMIN)) {
            throw new NotAuthorisedException("You are not authenticated, please try logging in again.");
        }
    }

    /**
     * Get token from X-Auth-Token or fallback to ?token=XXX query parameter.
     *
     * @param request ServletRequest to get token from
     * @return String token value
     */
    protected String getToken(final HttpServletRequest request) {
        String token = request.getHeader("X-Auth-Token");

        if (StringUtils.isEmpty(token) || "undefined".equals(token)) {
            // not in header
            token = request.getParameter("token");
        }

        return token;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {}

    /**
     * Handles exceptions thrown when finding images. Ensures
     * no JSON is serialized in the response.
     */
    @ExceptionHandler(ImageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void imageNotFoundException() {}

    /**
     * Handle exception thrown when retrieving images. Ensures
     * no JSON is serialized in the response.
     */
    @ExceptionHandler(ImageIOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void imageIoException() {}
}
