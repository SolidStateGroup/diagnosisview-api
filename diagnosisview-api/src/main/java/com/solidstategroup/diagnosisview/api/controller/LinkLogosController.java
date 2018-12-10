package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.LinkLogoDto;
import com.solidstategroup.diagnosisview.model.codes.LinkLogoRule;
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
 * Controller to manage logo rules based on a particular criteria.
 */
@Api(value = "/api/logo/rules", description = "Manage Logo Rules")
@RestController
@RequestMapping("/api/logo/rules")
@Log
public class LinkLogosController extends BaseController {

    private static final String IMAGE_URL_TEMPLATE = "/api/logo/rules/image/%s";

    private final LinkRulesService linkRulesService;

    public LinkLogosController(LinkRulesService linkRulesService) {

        this.linkRulesService = linkRulesService;
    }

    @ApiOperation(
            value = "Creates a link logo",
            response = LinkLogoDto.class)
    @PostMapping
    public LinkLogoDto addLinkLogoRule(
            HttpServletRequest request,
            @RequestBody @Validated LinkLogoDto linkLogoDto)
            throws Exception {

        isAdminUser(request);

        return buildLinkLogoRule(linkRulesService.addLogoRule(linkLogoDto));
    }

    @ApiOperation(
            value = "Fetches all link logo rules",
            response = LinkLogoDto.class,
            responseContainer = "List"
    )
    @GetMapping
    public List<LinkLogoDto> getAllLogoRules(HttpServletRequest request)
            throws Exception {

        isAdminUser(request);

        return linkRulesService
                .getLinkLogoRules()
                .stream()
                .map(this::buildLinkLogoRule)
                .collect(toList());
    }

    @ApiOperation(
            value = "Updates a logo rule",
            response = LinkLogoDto.class
    )
    @PutMapping("/{id}")
    public LinkLogoDto updateLogoTransformation(
            HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestBody LinkLogoDto linkLogoRuleDto)
            throws Exception {

        isAdminUser(request);

        return buildLinkLogoRule(linkRulesService.updateLogoRule(id, linkLogoRuleDto));
    }

    @ApiOperation(
            value = "Deletes a  logo rule for a given id"
    )
    @DeleteMapping("/{id}")
    public void deleteLinkLogo(
            HttpServletRequest request,
            @PathVariable("id") String id)
            throws Exception {

        isAdminUser(request);

        linkRulesService.deleteLinkLogoRule(id);
    }

    @ApiOperation(
            value = "Fetches a logo rule",
            response = LinkLogoDto.class
    )
    @GetMapping("/{id}")
    public LinkLogoDto getLogoRule(@PathVariable("id") final String id, HttpServletRequest request)
            throws Exception {

        isAdminUser(request);

        return buildLinkLogoRule(linkRulesService.getLinkLogoRule(id));
    }

    @ApiOperation(
            value = "Gets logo for a specified link logo id"
    )
    @GetMapping("image/{id}")
    public void getLinkLogoById(@PathVariable("id") final String id, HttpServletResponse response) {

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

    private LinkLogoDto buildLinkLogoRule(LinkLogoRule lt) {
        return new LinkLogoDto(
                lt.getId(), lt.getStartsWith(),
                lt.getLogoData(), lt.getLogoFileType(),
                String.format(IMAGE_URL_TEMPLATE, lt.getId()));
    }
}
