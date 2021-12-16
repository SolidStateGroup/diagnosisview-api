package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.exceptions.UsernameTakenException;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.PasswordResetDto;
import com.solidstategroup.diagnosisview.model.SavedUserCode;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.Utils;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.payloads.ForgotPasswordPayload;
import com.solidstategroup.diagnosisview.payloads.RegisterPayload;
import com.solidstategroup.diagnosisview.repository.UserRepository;
import com.solidstategroup.diagnosisview.results.FavouriteResult;
import com.solidstategroup.diagnosisview.results.HistoryResult;
import com.solidstategroup.diagnosisview.service.CaptchaValidatorService;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.EmailService;
import com.solidstategroup.diagnosisview.service.UserService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@inheritDoc}.
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final EmailService emailService;
  private final InstitutionService institutionService;
  private final CodeService codeService;
  private final CaptchaValidatorService captchaValidatorService;

  /**
   * Constructor for the dashboard user service.
   *
   * @param userRepository the repo to autowire
   */
  @Autowired
  public UserServiceImpl(final UserRepository userRepository,
      final EmailService emailService,
      final InstitutionService institutionService,
      final CodeService codeService,
      CaptchaValidatorService captchaValidatorService) {
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.institutionService = institutionService;
    this.codeService = codeService;
    this.captchaValidatorService = captchaValidatorService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addMultipleFavouritesToUser(User user, List<SavedUserCode> savedUserCodes)
      throws Exception {
    User savedUser = this.getUser(user.getUsername());
    HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
    if (savedUser.getFavourites() != null) {
      savedUser.getFavourites().forEach(savedCode -> savedCodesMap.put(
          savedCode.getLinkId() + savedCode.getCode() + savedCode.getType(), savedCode));
    }

    for (SavedUserCode savedUserCode : savedUserCodes) {
      validateFavourite(savedUserCode);
      if (!savedCodesMap
          .containsKey(
              savedUserCode.getLinkId() + savedUserCode.getCode() + savedUserCode.getType())) {
        savedCodesMap
            .put(savedUserCode.getLinkId() + savedUserCode.getCode() +
                savedUserCode.getType(), savedUserCode);
      }
    }

    savedUser.setFavourites(new ArrayList<>(savedCodesMap.values()));
    userRepository.save(savedUser);
    return savedUser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addFavouriteToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());

    validateFavourite(savedUserCode);

    HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
    if (savedUser.getFavourites() != null) {
      savedUser.getFavourites().forEach(savedCode -> savedCodesMap.put(
          savedCode.getLinkId() + savedCode.getCode() + savedCode.getType(),
          savedCode));
    }

    if (!savedCodesMap
        .containsKey(
            savedUserCode.getLinkId() + savedUserCode.getCode() + savedUserCode.getType())) {
      savedCodesMap
          .put(savedUserCode.getLinkId() + savedUserCode.getCode() + savedUserCode.getType(),
              savedUserCode);
    }

    savedUser.setFavourites(new ArrayList<>(savedCodesMap.values()));
    return userRepository.save(savedUser);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addMultipleHistoryToUser(User user, List<SavedUserCode> savedUserCodes)
      throws Exception {
    User savedUser = this.getUser(user.getUsername());
    List<SavedUserCode> userCodes = new ArrayList<>();

    if (savedUser.getHistory() != null) {
      savedUser.getHistory().forEach(history -> userCodes.add(history));
    }

    for (SavedUserCode savedUserCode : savedUserCodes) {
      if (savedUserCode.getDateAdded() == null) {
        savedUserCode.setDateAdded(new Date());
      }
      userCodes.add(savedUserCode);
    }

    savedUser.setHistory(new ArrayList(userCodes));
    return userRepository.save(savedUser);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User addHistoryToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());
    if (savedUserCode.getDateAdded() == null) {
      savedUserCode.setDateAdded(new Date());
    }
    List<SavedUserCode> userCodes = new ArrayList<>();

    if (savedUser.getHistory() != null) {
      savedUser.getHistory().forEach(history -> userCodes.add(history));
    }

    userCodes.add(savedUserCode);

    savedUser.setHistory(new ArrayList(userCodes));
    return userRepository.save(savedUser);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User registerUser(RegisterPayload payload) throws Exception {

    if (!captchaValidatorService.isValid(payload.getCaptchaResponse())) {
      throw new IllegalArgumentException("reCaptcha validation failed");
    }

    if (userRepository.findOneByUsername(payload.getUsername()) != null) {
      throw new UsernameTakenException();
    }

    User userToAdd = new User();
    userToAdd.setFirstName(payload.getFirstName());
    userToAdd.setLastName(payload.getLastName());
    userToAdd.setUsername(payload.getUsername());
    userToAdd.setEmailAddress(payload.getUsername());
    userToAdd.setDateCreated(new Date());
    userToAdd.setSalt(Utils.generateSalt());
    userToAdd.setPassword(DigestUtils.sha256Hex(payload.getPassword() +
        userToAdd.getStoredSalt()));
    userToAdd.setToken(UUID.randomUUID().toString());
    userToAdd.setRoleType(RoleType.USER);
    userToAdd.setOccupation(payload.getOccupation());

    // check make sure we have correct selected institution
    if (!StringUtils.isEmpty(payload.getInstitution())) {
      Institution institution = institutionService.getInstitution(payload.getInstitution());
      userToAdd.setInstitution(institution.getCode());
    }

    return userRepository.save(userToAdd);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User createOrUpdateUser(final User user, boolean isAdmin) throws Exception {
    // this is a new user
    if (user.getId() == null) {
      if (userRepository.findOneByUsername(user.getUsername()) != null) {
        throw new UsernameTakenException();
      }
      user.setUsername(user.getUsername());
      user.setEmailAddress(user.getEmailAddress());
      user.setDateCreated(new Date());
      user.setSalt(Utils.generateSalt());
      user.setPassword(DigestUtils.sha256Hex(user.getStoredPassword() +
          user.getStoredSalt()));
      user.setToken(UUID.randomUUID().toString());
      user.setRoleType(RoleType.USER);

      // check make sure we have correct selected institution
      if (!StringUtils.isEmpty(user.getInstitution())) {
        Institution institution = institutionService.getInstitution(user.getInstitution());
        user.setInstitution(institution.getCode());
      }

      return userRepository.save(user);

    } else {
      User savedUser;

      if (user.getId() != null) {
        savedUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new IllegalStateException("Could not find user"));
      } else {
        //Only certain fields can be updated, these are in this section.
        savedUser = userRepository.findOneByUsername(user.getUsername().toLowerCase());
      }

      if (user.getFirstName() != null) {
        savedUser.setFirstName(user.getFirstName());
      }

      if (user.getLastName() != null) {
        savedUser.setLastName(user.getLastName());
      }

      if (user.getOccupation() != null) {
        savedUser.setOccupation(user.getOccupation());
      }
      if (user.getInstitution() != null) {
        Institution institution = institutionService.getInstitution(user.getInstitution());
        savedUser.setInstitution(institution.getCode());
      }

      if (user.getEmailAddress() != null) {
        savedUser.setEmailAddress(user.getEmailAddress());
      }

      if (user.getExpiryDate() != null && isAdmin) {
        savedUser.setExpiryDate(user.getExpiryDate());
        if (user.getExpiryDate().after(new Date())) {
          savedUser.setActiveSubscription(true);
        }
      }

      if (user.getStoredPassword() != null) {

        // If the user isn't an admin, we need to ensure that the password matches the old one
        if (!isAdmin) {
          if (!Utils.checkPassword(user.getOldPassword(), savedUser.getStoredSalt(),
              savedUser.getStoredPassword())) {
            throw new BadCredentialsException("Current password incorrect. Please try again.");
          }
        }

        savedUser.setSalt(Utils.generateSalt());
        savedUser.setPassword(DigestUtils.sha256Hex(user.getStoredPassword() +
            savedUser.getStoredSalt()));
      }

      return userRepository.save(savedUser);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User saveUser(User user) {
    return userRepository.save(user);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User deleteUser(User user) {
    User user1 = userRepository.findOneByUsername(user.getUsername());
    user1.setDeleted(true);
    userRepository.save(user1);

    return user1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User deleteFavouriteToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());
    savedUser.getFavourites().removeIf(f ->
        f.getLinkId().equals(savedUserCode.getLinkId())
            && f.getCode().equalsIgnoreCase(savedUserCode.getCode())
            && f.getType().equalsIgnoreCase(savedUserCode.getType()));

    userRepository.save(savedUser);
    return savedUser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User deleteHistoryToUser(User user, SavedUserCode savedUserCode) throws Exception {
    User savedUser = this.getUser(user.getUsername());
    HashMap<String, SavedUserCode> savedCodesMap = new HashMap<>();
    savedUser.getHistory().forEach(
        savedCode -> savedCodesMap.put(savedCode.getCode() + savedCode.getType(), savedCode));

    if (!savedCodesMap.containsKey(savedUserCode.getCode() + savedUserCode.getType())) {
      savedCodesMap.remove(savedUserCode.getCode() + savedUserCode.getType());
    }

    savedUser.setHistory(new ArrayList<>(savedCodesMap.values()));
    userRepository.save(savedUser);
    return savedUser;
  }


  @Override
  @Transactional(readOnly = true)
  public List<FavouriteResult> getFavouriteList(final User user) throws ResourceNotFoundException {
    List<SavedUserCode> currentFavourites = user.getFavourites();

    if (CollectionUtils.isEmpty(currentFavourites)) {
      return new ArrayList<>();
    }

    List<String> favouritesCodes = currentFavourites.stream()
        .map(SavedUserCode::getCode)
        .collect(Collectors.toList());

    List<CodeDto> activeCodeDtos = codeService.getAllActiveByCodes(
        favouritesCodes, user.getInstitution());

    List<String> activeCodesCode = activeCodeDtos.stream()
        .map(CodeDto::getCode)
        .collect(Collectors.toList());

    List<SavedUserCode> filteredFavourites;

    // filter out removed and hidden codes
    filteredFavourites = currentFavourites.stream()
        .filter(f -> activeCodesCode.contains(f.getCode()))
        .collect(Collectors.toList());

    List<FavouriteResult> favouriteResults = new ArrayList<>();

    // Filter codes based on active codes and subscription
    filteredFavourites.forEach(f -> {
      // find active code
      Optional<CodeDto> dto = activeCodeDtos.stream()
          .filter(a -> a.getCode().equals(f.getCode()))
          .findFirst();

      if (dto.isPresent()) {
        Optional<LinkDto> linkDto = dto.get().getLinks().stream()
            .filter(l -> l.getId().equals(f.getLinkId()))
            .findFirst();

        if (linkDto.isPresent()) {
          // active Subscription add to favourite lists
          if (user.isActiveSubscription()) {
            favouriteResults.add(FavouriteResult.toResult(f, dto.get(), linkDto.get()));
          } else {
            // otherwise, ignore Red or Amber link unless link is marked as Free
            if ((linkDto.get().getFreeLink()
                || linkDto.get().getDifficultyLevel().getId()
                .equals(DifficultyLevel.GREEN.getId()))) {
              favouriteResults.add(FavouriteResult.toResult(f, dto.get(), linkDto.get()));
            }
          }
        }
      }
    });

    // sort by date added descending (latest first)
    favouriteResults.sort(Comparator.comparing(FavouriteResult::getDateAdded).reversed());

    // if user not subscribed, return only last 20
    return (favouriteResults.size() > 20 && !user.isActiveSubscription())
        ? favouriteResults.subList(0, favouriteResults.size() - 20)
        : favouriteResults;
  }

  @Override
  @Transactional(readOnly = true)
  public List<HistoryResult> getHistoryList(final User user) throws ResourceNotFoundException {

    List<SavedUserCode> currentHistory = user.getHistory();

    if (CollectionUtils.isEmpty(currentHistory)) {
      return new ArrayList<>();
    }

    List<String> historyCodes = currentHistory.stream()
        .map(SavedUserCode::getCode)
        .collect(Collectors.toList());

    // before returning list of History Items clean any that is hidden or removed externally
    List<CodeDto> activeCodeDtos = codeService.getAllActiveByCodes(
        historyCodes, user.getInstitution());

    List<String> activeCodesCode = activeCodeDtos.stream()
        .map(CodeDto::getCode)
        .collect(Collectors.toList());

    List<SavedUserCode> filteredHistory;
    // if history list is not the same as active codes we need to exclude from user History
    if (historyCodes.size() != activeCodeDtos.size()) {
      filteredHistory = currentHistory.stream()
          .filter(h -> activeCodesCode.contains(h.getCode()))
          .collect(Collectors.toList());
    } else {
      filteredHistory = currentHistory;
    }

    // if user not subscribed, return only last 20
    if (filteredHistory.size() > 20 && !user.isActiveSubscription()) {
      filteredHistory = filteredHistory.subList(filteredHistory.size() - 20,
          filteredHistory.size());
    }

    return filteredHistory.stream()
        .map(h -> {
          CodeDto dto = activeCodeDtos.stream()
              .filter(a -> a.getCode().equals(h.getCode()))
              .findFirst()
              .orElse(null);
          return HistoryResult.toResult(h, dto);
        }).collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User login(final String username, final String password) throws Exception {

    User user = userRepository.findOneByUsername(username.toLowerCase());

    if (user == null) {
      throw new BadCredentialsException("Login failed - please check your username and password.");
    }
    if (Utils.checkPassword(password, user.getStoredSalt(), user.getStoredPassword())) {
      if (user.isDeleted()) {
        throw new BadCredentialsException("This account has been deleted. " +
            "Please contact support@diagnosisview.org.");
      }

      //Admin users will always have a subscription
      if (user.getRoleType().equals(RoleType.ADMIN)) {
        user.setActiveSubscription(true);
        userRepository.save(user);
      }
      //If the user is not auto-renewing, and the expiry date has past, set them to inactive
      else if (!user.isAutoRenewing() && (user.getExpiryDate() == null ||
          user.getExpiryDate().before(new Date()))) {
        user.setActiveSubscription(false);
        userRepository.save(user);
      }

      return user;

    } else {

      throw new BadCredentialsException("Login failed - please check your username and password");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User getUser(final String username) {
    return userRepository.findOneByUsername(username);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<User> getExpiringUsers() throws Exception {
    return userRepository
        .findByExpiryDateLessThanEqualAndActiveSubscription(new DateTime().plusWeeks(1).toDate(),
            true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User getUserByToken(final String token) throws Exception {
    User user = userRepository.findOneByToken(token);

    //If no user is found, return null
    if (user == null) {
      return null;
    }

    //Admin users will always have a subscription
    if (user.getRoleType().equals(RoleType.ADMIN)) {
      user.setActiveSubscription(true);
      userRepository.save(user);
    }
    //If the user is not auto-renewing, and the expiry date has past, set them to inactive
    else if (!user.isAutoRenewing() && (user.getExpiryDate() == null || user.getExpiryDate()
        .before(new Date()))) {
      user.setActiveSubscription(false);
      userRepository.save(user);
    }

    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<User> getAllUsers() throws Exception {
    return userRepository.findAll(new Sort(Sort.Direction.ASC, "dateCreated"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public void sendResetPassword(ForgotPasswordPayload payload) throws Exception {

    if (!captchaValidatorService.isValid(payload.getCaptchaResponse())) {
      throw new IllegalArgumentException("reCaptcha validation failed");
    }

    User existingUser = userRepository.findOneByUsername(payload.getUsername());

    if (existingUser == null) {
      return;
    }

    if (existingUser.getResetExpiryDate() == null
        || existingUser.getResetExpiryDate().before(new Date())) {
      log.info("Sending email....");

      String generatedString = RandomStringUtils.random(6, true, true).toUpperCase();

      existingUser.setResetCode(generatedString);
      DateTime oneDayAdded = new DateTime().plusHours(1);
      existingUser.setResetExpiryDate(oneDayAdded.toDate());
      userRepository.save(existingUser);
      emailService.sendForgottenPasswordEmail(existingUser, existingUser.getResetCode());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public void resetPassword(final PasswordResetDto resetDto) throws Exception {
    //Get the user from the db
    User user = this.getUser(resetDto.getUsername());

    //If the user doesnt exist, throw an error
    if (user == null) {
      throw new BadRequestException("We were unable to validate your request. " +
          "Please check your username and reset code.");
    }

    //Check the reset code hasn't expired
    if (user.getResetExpiryDate().before(new Date())) {
      throw new BadRequestException("Your request has expired. Please request a new reset code");
    }
    //Check the reset code is ok
    if (!user.getResetCode().equals(resetDto.getResetCode().toUpperCase())) {
      throw new BadRequestException("We were unable to validate your request. " +
          "Please check your username and reset code.");
    }

    //Update the password and salt
    user.setResetExpiryDate(null);
    user.setResetCode(null);
    user.setSalt(Utils.generateSalt());
    user.setPassword(DigestUtils.sha256Hex(resetDto.getNewPassword() + user.getStoredSalt()));
    userRepository.save(user);
  }

  /**
   * Validate give given favourite object
   *
   * @param favourite a favourite to validate
   * @throws Exception when failed validation
   */
  private void validateFavourite(SavedUserCode favourite) throws Exception {

    if (favourite.getLinkId() == null) {
      log.error("Missing link id from favourite");
      throw new Exception("Missing link id for favourite");
    }

    if (StringUtils.isEmpty(favourite.getCode())) {
      log.error("Missing code from favourite");
      throw new Exception("Missing code for favourite");
    }

    if (StringUtils.isEmpty(favourite.getType())) {
      log.error("Missing type from favourite");
      throw new Exception("Missing type for Diagnosis Code");
    }
  }

}
