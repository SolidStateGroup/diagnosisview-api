package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
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
import java.util.Map;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@Api(value = "/api/user", description = "Manage Users")
@RequestMapping("/api/user")
@Log
public class ValidateApiController extends BaseController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService UserService manages the dashboard users
     */
    @Autowired
    public ValidateApiController(final UserService userService) {
        super();
        this.userService = userService;
    }

    /**
     * Validates and android receipt against the play store API
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/validate/android", method = RequestMethod.POST)
    @ApiOperation(value = "Validate an Android receipt",
            notes = "Validate an Android receipt",
            response = SavedUserCode.class)
    public User validateAndroidReceipt(@RequestBody final String purchase,
                                     final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = this.getUserFromRequest(request);
        if (user == null) {
            throw new Exception("You are not authenticated, please login to save favourites");
        }

        return userService.verifyAndroidToken(user, purchase);
    }

    /**
     * Delete a history item for the user.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/validate/ios", method = RequestMethod.POST)
    @ApiOperation(value = "Validate and iOS receipt",
            notes = "Validates and iOS receipt against",
            response = SavedUserCode.class)
    public User validateIosReceipt(@RequestBody final Map<String, String> purchase,
                               final HttpServletRequest request) throws Exception {
        //Get the user from the request
        User user = this.getUserFromRequest(request);
        if (user == null) {
            throw new Exception("You are not authenticated, please login to save favourites");
        }

        return userService.verifyAppleReceiptData(user, purchase.get("transactionReceipt"));
    }

}
