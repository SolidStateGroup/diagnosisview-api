package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.exceptions.ImageIOException;
import com.solidstategroup.diagnosisview.exceptions.ImageNotFoundException;
import com.solidstategroup.diagnosisview.model.LogoRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Controller to manage logo rules based on a particular criteria.
 */
@Slf4j
@RestController
@RequestMapping("/api/logo/rules")
@Api(value = "/api/logo/rules", description = "Manage Logo Rules")
public class LogoRulesController extends BaseController {

    private static final String IMAGE_URL_TEMPLATE = "/api/logo/rules/%s/image";

    private final LogoRulesService logoRulesService;

    public LogoRulesController(UserService userService,
                               LogoRulesService logoRulesService) {

        super(userService);
        this.logoRulesService = logoRulesService;
    }

    @ApiOperation(
            value = "Creates a logo rule",
            response = LogoRuleDto.class)
    @PostMapping
    public LogoRuleDto add(
            HttpServletRequest request,
            @RequestBody @Validated LogoRuleDto logoRule)
            throws Exception {

        isAdminUser(request);

        return buildLogoRule(
                logoRulesService.add(logoRule));
    }

    @ApiOperation(
            value = "Fetches all logo rules",
            response = LogoRuleDto.class,
            responseContainer = "List"
    )
    @GetMapping
    public List<LogoRuleDto> getAll(HttpServletRequest request)
            throws Exception {

        checkIsAuthenticated(request);

        return logoRulesService
                .getRules()
                .stream()
                .map(this::buildLogoRule)
                .collect(toList());
    }

    @ApiOperation(
            value = "Updates a logo rule",
            response = LogoRuleDto.class
    )
    @PutMapping("/{id}")
    public LogoRuleDto update(
            HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestBody LogoRuleDto logoRule)
            throws Exception {

        isAdminUser(request);

        return buildLogoRule(logoRulesService.update(id, logoRule));
    }

    @ApiOperation(
            value = "Deletes a logo rule for a given id"
    )
    @DeleteMapping("/{id}")
    public void delete(
            HttpServletRequest request,
            @PathVariable("id") String id)
            throws Exception {

        isAdminUser(request);

        logoRulesService.delete(id);
    }

    @ApiOperation(
            value = "Fetches a logo rule",
            response = LogoRuleDto.class
    )
    @GetMapping("/{id}")
    public LogoRuleDto get(@PathVariable("id") final String id, HttpServletRequest request)
            throws Exception {

        isAdminUser(request);

        return buildLogoRule(logoRulesService.get(id));
    }

    @ApiOperation(
            value = "Gets logo for a specified link logo id",
            notes = "Endpoint will throw a plain 404 if the logo rule is not found" +
                    "and a 500 if there is an IO exception thrown when retrieving the image."
    )
    @GetMapping("/{id}/image")
    public void getImage(
            @PathVariable("id") final String id,
            HttpServletResponse response) {

        LogoRule logoRule = logoRulesService.get(id);

        if (logoRule == null) {

            throw new ImageNotFoundException("Link rule not found");
        }

        try (InputStream is = new ByteArrayInputStream(logoRule.getLinkLogo())) {

            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ioe) {

            log.error("Unable to read image %s", logoRule.getLinkLogo());
            throw new ImageIOException(ioe);
        }

        response.setStatus(HttpStatus.OK.value());
    }

    private LogoRuleDto buildLogoRule(LogoRule lt) {
        return new LogoRuleDto(
                lt.getId(),
                lt.getStartsWith(),
                lt.getLogoData(),
                lt.getLogoFileType(),
                String.format(IMAGE_URL_TEMPLATE, lt.getId()),
                lt.getOverrideDifficultyLevel());
    }
}
