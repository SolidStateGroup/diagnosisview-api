package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.ExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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

    private final UserService userService;
    private final CodeService codeService;
    private final LinkService linkService;
    private final ExternalStandardRepository externalStandardRepository;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService UserService manages the dashboard users
     * @param linkService
     */
    @Autowired
    public AdminApiController(final UserService userService,
                              final CodeService codeService,
                              final LinkService linkService,
                              final ExternalStandardRepository externalStandardRepository) {
        super();
        this.userService = userService;
        this.codeService = codeService;
        this.linkService = linkService;
        this.externalStandardRepository = externalStandardRepository;
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

        if (loggedInUser == null || loggedInUser.getRoleType().equals(RoleType.USER)) {
            throw new IllegalStateException("You are not authenticated. Please contact support.");
        }
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

        isAdminUser(request);

        return userService.getAllUsers();
    }

    /**
     * Update a user.
     *
     * @param user User user to update
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/user", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete User - TEST PURPOSES ONLY",
            notes = "Pass the user in with an ID to be deleted")
    public User deleteUser(@RequestBody final User user,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

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
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ApiOperation(value = "Create User",
            notes = "Create a user, pass the password in which will then be encrypted",
            response = User.class)
    public User createUser(@RequestBody final User user,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return userService.createOrUpdateUser(user, true);
    }

    /**
     * Create a code within DV.
     *
     * @param code - code to create
     * @return the created code with ID
     */
    @RequestMapping(value = "/code", method = RequestMethod.POST)
    @ApiOperation(value = "Create Code",
            notes = "Creates code within DV (unsure if required)",
            response = Code.class)
    public Code createCode(@RequestBody final Code code,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return codeService.upsertCode(code, false);
    }

    /**
     * @param code
     * @return
     */
    @RequestMapping(value = "/code", method = RequestMethod.PUT)
    @ApiOperation(value = "Update Code",
            notes = "Update a user, pass the password in which will then be encrypted",
            response = Code.class)
    public Code updateCode(@RequestBody final Code code,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return codeService.upsertCode(code, false);
    }


    /**
     * Update a DV link
     *
     * @param link Link the link the update
     * @return Link the updated Link
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/code/link", method = RequestMethod.PUT)
    @ApiOperation(value = "Update Link",
            notes = "Updates a link with DV editable fields.",
            response = Link.class)
    public Link updateLink(@RequestBody final Link link,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        Link existingLink = linkService.getLink(link.getId());

        if (existingLink == null) {
            throw new BadRequestException("The link does not exist within DiagnosisView.");

        }
        return linkService.saveLink(link);
    }

    /**
     * Return all external standards within PV
     *
     * @return List the external standards within diagnosisview
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/code/external-standards", method = RequestMethod.GET)
    @ApiOperation(value = "Get External Standards",
            notes = "Get all external standards within DV",
            response = User.class)
    public List<ExternalStandard> getExternalStandards(HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return externalStandardRepository.findAll();
    }

    /**
     * Update a user.
     *
     * @param user User user to create
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.PUT)
    @ApiOperation(value = "Create User",
            notes = "Create a user, pass the password in which will then be encrypted",
            response = User.class)
    public User updateUser(@PathVariable("userId") final Long userId,
                           @RequestBody final User user,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        user.setId(userId);

        return userService.createOrUpdateUser(user, true);
    }

}
