package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.payloads.ChargebeeSubscribePayload;
import com.solidstategroup.diagnosisview.results.ChargebeeHostedPageResult;
import com.solidstategroup.diagnosisview.service.SubscriptionService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "/api/user", description = "Manage Users subscriptions")
@RequestMapping("/api/user")
public class SubscriptionController extends BaseController {

  private final SubscriptionService subscriptionService;

  public SubscriptionController(final UserService userService,
      SubscriptionService subscriptionService) {
    super(userService);
    this.subscriptionService = subscriptionService;
  }

  @ApiOperation(value = "Validate an Android receipt",
      notes = "Validates and android receipt against the play store API",
      response = User.class)
  @PostMapping("/validate/android")
  public User validateAndroidReceipt(@RequestBody final String purchase,
      final HttpServletRequest request) throws Exception {
    User user = checkIsAuthenticated(request);

    return subscriptionService.verifyAndroidToken(user, purchase);
  }

  @ApiOperation(value = "Validate an Android receipt",
      notes = "Validates and android receipt against the play store API.",
      response = String.class)
  @PostMapping("/validate/android/public")
  public String validateAndroidReceiptPublic(@RequestBody final String purchase) throws Exception {

    return subscriptionService.verifyAndroidToken(purchase);
  }

  @ApiOperation(value = "Validate and iOS receipt",
      notes = "Validates and iOS receipt against",
      response = User.class)
  @RequestMapping(value = "/validate/ios", method = RequestMethod.POST)
  public User validateIosReceipt(@RequestBody final Map<String, String> purchase,
      final HttpServletRequest request) throws Exception {

    User user = checkIsAuthenticated(request);

    return subscriptionService.verifyAppleReceiptData(user, purchase.get("transactionReceipt"));
  }

  @ApiOperation(value = "Subscribe user to Chargebee",
      notes = "Subscribes current user to Chargebee",
      response = User.class)
  @PostMapping("/subscriptions/chargebee")
  public User validateChargebeeSubscription(@RequestBody @Valid ChargebeeSubscribePayload payload,
      final HttpServletRequest request) throws Exception {
    User requestUser = checkIsAuthenticated(request);
    return subscriptionService.validateChargebee(requestUser, payload);
  }

  @ApiOperation(value = "Chargebee hosted page",
      notes = "Get Chargebee hosted page for current user subscription",
      response = User.class)
  @GetMapping("/subscriptions/chargebee/hosted-page")
  public ChargebeeHostedPageResult chargebeeHostedPage(final HttpServletRequest request) throws Exception {
    User requestUser = checkIsAuthenticated(request);
    return subscriptionService.getChargebeeHostedPage(requestUser);
  }
}
