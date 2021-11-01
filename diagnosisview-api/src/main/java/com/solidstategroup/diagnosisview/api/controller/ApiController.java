package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.LoginRequest;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.payloads.RegisterPayload;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Api(value = "provides user management endpoints")
public class ApiController extends BaseController {

  public ApiController(final UserService userService) {

    super(userService);
  }

  @ApiOperation(value = "User login to system")
  @PostMapping("/login")
  public User login(@RequestBody @Validated LoginRequest loginRequest) throws Exception {

    return userService.login(loginRequest.getUsername(), loginRequest.getPassword());
  }

  @ApiOperation(value = "Get the current user that is logged into the api")
  @GetMapping("/account")
  public User getAccount(final HttpServletRequest request) throws Exception {

    return checkIsAuthenticated(request);
  }

  @ApiOperation(value = "User wants to register")
  @PostMapping("/register")
  public User register(@Valid @RequestBody RegisterPayload payload) throws Exception {
    return userService.registerUser(payload);
  }
}
