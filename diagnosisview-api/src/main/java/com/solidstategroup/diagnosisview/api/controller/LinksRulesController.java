package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.service.LinkRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Controller to manage link rules based on a particular criteria.
 */
@Api(value = "/api/link/rules", description = "Manage Link Rules")
@RestController
@RequestMapping("/api/link/rules")
public class LinksRulesController extends BaseController {

    private final LinkRuleService linkRuleService;

    public LinksRulesController(LinkRuleService linkRuleService) {
        this.linkRuleService = linkRuleService;
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
                linkRuleService.addRule(linkRule));
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

        return linkRuleService
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
                linkRuleService.getLinkRule(id));
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

        return buildLinkRuleDto(
                linkRuleService.updateLinkRule(id, linkTransformationDto));
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

        linkRuleService.deleteLinkRule(id);
    }

    /**
     * Converts between internal {@link LinkRule} to {@link LinkRuleDto}
     * @param lt {@link LinkRule} for repository
     * @return Dto to send to FE
     */
    private LinkRuleDto buildLinkRuleDto(LinkRule lt) {
        return new LinkRuleDto(lt.getId(), lt.getLink(), lt.getTransform(), lt.getCriteriaType(), lt.getCriteria());
    }
}
