package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.LinkMappingDto;
import com.solidstategroup.diagnosisview.model.codes.LinkMapping;
import com.solidstategroup.diagnosisview.service.LinkMappingService;
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
 * Controller to manage link mapping base on a
 * particular criteria.
 */
@Api(value = "/api/links/mapping", description = "Manage Link Mappings")
@RestController
@RequestMapping("/api/links/mapping")
public class LinksMappingController extends BaseController {

    private final LinkMappingService linkMappingService;

    public LinksMappingController(LinkMappingService linkMappingService) {
        this.linkMappingService = linkMappingService;
    }

    @ApiOperation(
            value = "Creates a mapping for a link",
            response = LinkMappingDto.class)
    @PostMapping
    public LinkMappingDto addLinkTransformation(
            HttpServletRequest request,
            @RequestBody @Validated LinkMappingDto linkTransformation)
            throws Exception {

        isAdminUser(request);

        return buildLinkTransformationDto(
                linkMappingService.addLinkTransformation(linkTransformation));
    }

    @ApiOperation(
            value = "Returns all link mappings",
            response = LinkMappingDto.class,
            responseContainer = "List"
    )
    @GetMapping
    public List<LinkMappingDto> getAllLinkTransformations(HttpServletRequest request)
            throws Exception {

        isAdminUser(request);

        return linkMappingService
                .getLinkTransformations()
                .stream()
                .map(this::buildLinkTransformationDto)
                .collect(toList());
    }

    @ApiOperation(
            value = "Returns a link mapping for a given uuid",
            response = LinkMappingDto.class
    )
    @GetMapping("/{uuid}")
    public LinkMappingDto getLinkTransformation(
            HttpServletRequest request,
            @PathVariable("uuid") String uuid)
            throws Exception {

        isAdminUser(request);

        return buildLinkTransformationDto(
                linkMappingService.getLinkTransformation(uuid));
    }

    @ApiOperation(
            value = "Updates a link mapping",
            response = LinkMappingDto.class
    )
    @PutMapping("/{uuid}")
    public LinkMappingDto updateLinkTransformation(
            HttpServletRequest request,
            @PathVariable("uuid") String uuid,
            @RequestBody LinkMappingDto linkTransformationDto)
            throws Exception {

        isAdminUser(request);

        return buildLinkTransformationDto(linkMappingService.updateLinkTransformation(uuid, linkTransformationDto));
    }

    @ApiOperation(
            value = "Deletes a link mapping for a given uuid"
    )
    @DeleteMapping("/{uuid}")
    public void deleteLinkTransformation(
            HttpServletRequest request,
            @PathVariable("uuid") String uuid)
            throws Exception {

        isAdminUser(request);

        linkMappingService.deleteLinkTransformation(uuid);
    }

    private LinkMappingDto buildLinkTransformationDto(LinkMapping lt) {
        return new LinkMappingDto(lt.getId(), lt.getLink(), lt.getTransform(), lt.getInstitution());
    }
}
