package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.LinkLogoDto;
import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkLogoRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.service.LinkRulesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
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
 * Controller to manage link rules based on a particular criteria.
 */
@Api(value = "/api/link/rules", description = "Manage Link Rules")
@RestController
@RequestMapping("/api/link/rules")
@Log
public class LinksRulesController extends BaseController {

    private final LinkRulesService linkRulesService;

    public LinksRulesController(LinkRulesService linkRulesService) {
        this.linkRulesService = linkRulesService;
    }


    @ApiOperation(
            value = "Creates a link rule",
            response = LinkRuleDto.class)
    @PostMapping
    public LinkRuleDto addLinkRule(
            HttpServletRequest request,
            @RequestBody @Validated LinkRuleDto linkRule)
            throws Exception {

        isAdminUser(request);

        return buildLinkRuleDto(
                linkRulesService.addRule(linkRule));
    }

    @ApiOperation(
            value = "Creates a link logo",
            response = LinkLogoDto.class)
    @PostMapping("/logo")
    public LinkLogoDto addLinkLogoRule(
            HttpServletRequest request,
            @RequestBody @Validated LinkLogoDto linkLogoDto)
            throws Exception {

        isAdminUser(request);

        return buildLinkLogoRule(linkRulesService.addLogoRule(linkLogoDto));
    }

    @ApiOperation(
            value = "Fetches all link rules",
            response = LinkRuleDto.class,
            responseContainer = "List"
    )
    @GetMapping
    public List<LinkRuleDto> getAllRules(HttpServletRequest request)
            throws Exception {

        isAdminUser(request);

        return linkRulesService
                .getLinkRules()
                .stream()
                .map(this::buildLinkRuleDto)
                .collect(toList());
    }

    @ApiOperation(
            value = "Fetches a link rule for a given uuid",
            response = LinkRuleDto.class
    )
    @GetMapping("/{id}")
    public LinkRuleDto getLinkRule(
            HttpServletRequest request,
            @PathVariable("id") String id)
            throws Exception {

        isAdminUser(request);

        return buildLinkRuleDto(
                linkRulesService.getLinkRule(id));
    }

    @ApiOperation(
            value = "Updates a link rule",
            response = LinkRuleDto.class
    )
    @PutMapping("/{id}")
    public LinkRuleDto updateLinkTransformation(
            HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestBody LinkRuleDto linkTransformationDto)
            throws Exception {

        isAdminUser(request);

        return buildLinkRuleDto(linkRulesService.updateLinkRule(id, linkTransformationDto));
    }


    @ApiOperation(
            value = "Updates a link logo rule",
            response = LinkLogoDto.class
    )
    @PutMapping("/logo/{id}")
    public LinkLogoDto updateLogoTransformation(
            HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestBody LinkLogoDto linkLogoRuleDto)
            throws Exception {

        isAdminUser(request);

        return buildLinkLogoRule(linkRulesService.updateLogoRule(id, linkLogoRuleDto));
    }

    @ApiOperation(
            value = "Deletes a link logo rule for a given id"
    )
    @DeleteMapping("/logo/{id}")
    public void deleteLinkLogo(
            HttpServletRequest request,
            @PathVariable("id") String id)
            throws Exception {

        isAdminUser(request);

        linkRulesService.deleteLinkRule(id);
    }

    @ApiOperation(
            value = "Deletes a link rule for a given id"
    )
    @DeleteMapping("/{id}")
    public void deleteLinkTransformation(
            HttpServletRequest request,
            @PathVariable("id") String id)
            throws Exception {

        isAdminUser(request);

        linkRulesService.deleteLinkRule(id);
    }

    @ApiOperation(
            value = "Gets logo for a specified link logo id"
    )
    @GetMapping("/link/logo/{id}")
    public void getLinkLogoById(@PathVariable("id") final String id, HttpServletResponse response)
            throws Exception {
        InputStream is = null;
        try {
            LinkLogoRule linkLogoRule = linkRulesService.getLinkLogoRule(id);
            response.setContentType(linkLogoRule.getLogoFileType());
            is = new ByteArrayInputStream(linkLogoRule.getLinkLogo());

            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.severe("Failed to close input stream {}" + e.getMessage());
                }
            }
        }
    }

    private LinkRuleDto buildLinkRuleDto(LinkRule lt) {
        return new LinkRuleDto(lt.getId(), lt.getLink(), lt.getTransform(), lt.getInstitution());
    }

    private LinkLogoDto buildLinkLogoRule(LinkLogoRule lt) {
        return new LinkLogoDto(lt.getId(), lt.getStartsWith(), lt.getLogoData(), lt.getLogoFileType());
    }
}
