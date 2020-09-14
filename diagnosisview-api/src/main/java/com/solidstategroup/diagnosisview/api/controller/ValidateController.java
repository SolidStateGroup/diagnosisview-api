package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Api(value = "/api/user", description = "Manage Users")
@RequestMapping("/api/user")
public class ValidateController extends BaseController {

    public ValidateController(final UserService userService) {

        super(userService);
    }

    @ApiOperation(value = "Validate an Android receipt",
            notes = "Validates and android receipt against the play store API",
            response = User.class)
    @PostMapping("/validate/android")
    public User validateAndroidReceipt(@RequestBody final String purchase,
                                       final HttpServletRequest request) throws Exception {
        User user = checkIsAuthenticated(request);

        return userService.verifyAndroidToken(user, purchase);
    }

    @ApiOperation(value = "Validate an Android receipt",
            notes = "Validates and android receipt against the play store API.",
            response = User.class)
    @PostMapping("/validate/android/public")
    public User validateAndroidReceiptPublic(@RequestBody final String purchase,
                                       final HttpServletRequest request) throws Exception {
        User user = checkIsAuthenticated(request);

        return userService.verifyAndroidToken(user, purchase);
    }

    @ApiOperation(value = "Validate and iOS receipt",
            notes = "Validates and iOS receipt against",
            response = User.class)
    @RequestMapping(value = "/validate/ios", method = RequestMethod.POST)
    public User validateIosReceipt(@RequestBody final Map<String, String> purchase,
                                   final HttpServletRequest request) throws Exception {

        User user = checkIsAuthenticated(request);

        return userService.verifyAppleReceiptData(user, purchase.get("transactionReceipt"));
    }
}
