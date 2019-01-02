package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.ExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@Api(value = "Secured API controller")
public class AdminApiController extends BaseController {

    private final CodeService codeService;
    private final LinkService linkService;
    private final ExternalStandardRepository externalStandardRepository;

    public AdminApiController(final UserService userService,
                              final CodeService codeService,
                              final LinkService linkService,
                              final ExternalStandardRepository externalStandardRepository) {

        super(userService);
        this.codeService = codeService;
        this.linkService = linkService;
        this.externalStandardRepository = externalStandardRepository;
    }

    @ApiOperation(value = "Logs user into the system")
    @PostMapping(value = "/login")
    public User login(@RequestBody final User user) throws Exception {

        User loggedInUser = userService.login(user.getUsername(), user.getStoredPassword());

        if (loggedInUser == null || loggedInUser.getRoleType().equals(RoleType.USER)) {
            throw new IllegalStateException("You are not authenticated. Please contact support.");
        }
        log.info("Logging in Admin - " + loggedInUser.getUsername());
        return loggedInUser;
    }

    @ApiOperation(value = "Get All Users",
            notes = "Admin User endpoint to get all users within the DiagnosisView",
            response = User.class,
            responseContainer = "List")
    @GetMapping(value = "/users")
    public List<User> getAllUsers(HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return userService.getAllUsers();
    }

    @ApiOperation(value = "Delete User - TEST PURPOSES ONLY",
            notes = "Pass the user in with an ID to be deleted")
    @DeleteMapping(value = "/user")
    public User deleteUser(@RequestBody final User user,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        //Soft delete, making user as deleted
        return userService.deleteUser(user);
    }

    @ApiOperation(value = "Create User",
            notes = "Create a user, pass the password in which will then be encrypted",
            response = User.class)
    @PostMapping(value = "/user")
    public User createUser(@RequestBody final User user,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return userService.createOrUpdateUser(user, true);
    }

    @ApiOperation(value = "Create Code",
            notes = "Creates code within DV",
            response = Code.class)
    @PostMapping(value = "/code")
    public Code createCode(@RequestBody final Code code,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return codeService.upsert(code, false);
    }


    @ApiOperation(value = "Updates Code",
            notes = "Updates a code within DV",
            response = Code.class)
    @PutMapping(value = "/code")
    public Code updateCode(@RequestBody final Code code,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return codeService.upsert(code, false);
    }

    @ApiOperation(value = "Update Link",
            notes = "Updates a link with DV editable fields.",
            response = Link.class)
    @PutMapping(value = "/code/link")
    public Link updateLink(@RequestBody final Link link,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return linkService.update(link);
    }

    @ApiOperation(value = "Get External Standards",
            notes = "Get all external standards within DV",
            response = ExternalStandard.class,
            responseContainer = "List")
    @GetMapping(value = "/code/external-standards")
    public List<ExternalStandard> getExternalStandards(HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return externalStandardRepository.findAll();
    }

    @ApiOperation(value = "Create User",
            notes = "Create a user, pass the password in which will then be encrypted",
            response = User.class)
    @PutMapping(value = "/user/{userId}")
    public User updateUser(@PathVariable("userId") final Long userId,
                           @RequestBody final User user,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        user.setId(userId);

        return userService.createOrUpdateUser(user, true);
    }
}
