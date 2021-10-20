package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.FeedbackDto;
import com.solidstategroup.diagnosisview.model.PasswordResetDto;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.results.HistoryResult;
import com.solidstategroup.diagnosisview.service.EmailService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Api(value = "/api/user")
public class UserController extends BaseController {

  private final EmailService emailService;

  public UserController(final UserService userService,
      final EmailService emailService) {
    super(userService);

    this.emailService = emailService;
  }

  @ApiOperation(value = "Update User",
      notes = "Update a user, pass the password in which will then be encrypted",
      response = User.class)
  @PutMapping("/")
  public User updateUser(@RequestBody final User user,
      final HttpServletRequest request) throws Exception {

    User requestUser = checkIsAuthenticated(request);

    user.setId(requestUser.getId());
    user.setUsername(requestUser.getUsername());

    return userService.createOrUpdateUser(user, false);
  }

  @ApiOperation(value = "Feedback",
      notes = "Sends feedback to DV team")
  @PostMapping("/feedback")
  public void sendDVFeedback(@RequestBody final FeedbackDto feedbackDto,
      final HttpServletRequest request) throws Exception {

    User user = getUserFromRequest(request);

    emailService.sendFeedback(user, feedbackDto.getBody());
  }

  @ApiOperation(value = "Reset password",
      notes = "Reset the password of a user with the given reset code")
  @PostMapping("/reset-password")
  public void resetPassword(@Valid @RequestBody final PasswordResetDto passwordResetDto)
      throws Exception {

    userService.resetPassword(passwordResetDto);
  }

  @ApiOperation(value = "Forgotten password",
      notes = "Sends a reset password email to a user")
  @PostMapping("/forgotten-password")
  public void forgottenPassword(@RequestBody final User user) throws Exception {

    userService.sendResetPassword(userService.getUser(user.getUsername()));
  }

  @ApiOperation(value = "Get a list of favourite codes",
      notes = "Get a list of user's favourite codes",
      response = User.class)
  @GetMapping("/favourites")
  public List<SavedUserCode> getFavouriteList(final HttpServletRequest request) throws Exception {
    User user = checkIsAuthenticated(request);

    return userService.getFavouriteList(user);
  }

  @ApiOperation(value = "Get a list of history",
      notes = "Get a list of user's history",
      response = User.class)
  @GetMapping("/history")
  public List<HistoryResult> getHistoryList(final HttpServletRequest request) throws Exception {
    User user = checkIsAuthenticated(request);
    return userService.getHistoryList(user);
  }

  @ApiOperation(value = "Add a code to favourites",
      notes = "Adds a code to user favourites",
      response = User.class)
  @PutMapping("/favourites")
  public User saveFavourite(@RequestBody final SavedUserCode favourite,
      final HttpServletRequest request) throws Exception {

    User user = checkIsAuthenticated(request);

    return userService.addFavouriteToUser(user, favourite);
  }

  @ApiOperation(value = "Save user history",
      notes = "Sync multiple history items for a user",
      response = User.class)
  @PutMapping("/sync/history")
  public User syncHistory(@RequestBody final List<SavedUserCode> historyList,
      final HttpServletRequest request) throws Exception {

    User user = checkIsAuthenticated(request);

    return userService.addMultipleHistoryToUser(user, historyList);
  }

  @ApiOperation(value = "Save users favourites",
      notes = "Sync multiple favourites for a user",
      response = User.class)
  @PutMapping("/sync/favourites")
  public User syncFavourites(@RequestBody final List<SavedUserCode> favouriteList,
      final HttpServletRequest request) throws Exception {

    User user = checkIsAuthenticated(request);

    return userService.addMultipleFavouritesToUser(user, favouriteList);
  }

  @ApiOperation(value = "Save user history",
      notes = "Add a history item to users history",
      response = User.class)
  @PutMapping("/history")
  public User saveHistory(@RequestBody final SavedUserCode history,
      final HttpServletRequest request) throws Exception {

    User user = checkIsAuthenticated(request);

    return userService.addHistoryToUser(user, history);
  }

  @ApiOperation(value = "Delete a code from favourites",
      notes = "Deletes a code from user favourites",
      response = User.class)
  @DeleteMapping("/favourites")
  public User deleteFavourite(@RequestBody final SavedUserCode favourite,
      final HttpServletRequest request) throws Exception {

    User user = checkIsAuthenticated(request);

    return userService.deleteFavouriteToUser(user, favourite);
  }

  @ApiOperation(value = "Delete user history item",
      notes = "Remove a history item to users history",
      response = User.class)
  @DeleteMapping("/history")
  public User deleteHistoryItem(@RequestBody final SavedUserCode history,
      final HttpServletRequest request) throws Exception {

    User user = this.checkIsAuthenticated(request);

    return userService.deleteHistoryToUser(user, history);
  }
}
