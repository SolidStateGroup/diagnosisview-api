package com.solidstategroup.diagnosisview.api.controller;

import com.google.api.client.util.Base64;
import com.solidstategroup.diagnosisview.exceptions.ImageIOException;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.service.impl.InstitutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * CRUD admin endpoints for Institution lookup types
 */
@Slf4j
@RestController
@RequestMapping("/api/institutions")
@Api(value = "CRUD admin controller for Institution")
public class InstitutionController extends BaseController {

    private final InstitutionService institutionService;

    public InstitutionController(final UserService userService,
                                 final InstitutionService institutionService) {

        super(userService);
        this.institutionService = institutionService;
    }


    @ApiOperation(value = "Create Institution",
            notes = "Create new institution in the system",
            response = User.class)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Institution create(@RequestBody final Institution payload,
                              HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return institutionService.create(payload);
    }

    @ApiOperation(value = "Update Institution",
            notes = "Update institution details in the system",
            response = User.class)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Institution update(@PathVariable("id") final Long id,
                              @RequestBody final Institution payload,
                              HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return institutionService.update(id, payload);
    }

    @ApiOperation(value = "Get Institution",
            notes = "Get institution details in the system",
            response = User.class)
    @GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Institution get(@PathVariable("id") final Long id,
                           HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return institutionService.get(id);
    }

    @ApiOperation(value = "Delete Institution",
            notes = "Delete institution from the system",
            response = User.class)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@PathVariable("id") final Long id,
                                 HttpServletRequest request) throws Exception {

        isAdminUser(request);

        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Get All Institutions",
            notes = "Admin User endpoint to get all institutions within the DiagnosisView",
            response = Institution.class,
            responseContainer = "List")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Institution> getAll(HttpServletRequest request) throws Exception {

        isAdminUser(request);

        return institutionService.getAll();
    }


    /**
     * Serve logo image for Institution
     *
     * @param id
     * @param response
     */
    @ApiOperation(
            value = "Gets logo for a specified Institution id",
            notes = "Endpoint will throw a plain 404 if the logo rule is not found" +
                    "and a 500 if there is an IO exception thrown when retrieving the image."
    )
    @GetMapping("/{id}/logo")
    public void getImage(@PathVariable("id") final Long id, HttpServletResponse response)
            throws ResourceNotFoundException {

        Institution institution = institutionService.get(id);

        if (institution.getLogoData() != null) {
            try (InputStream is = new ByteArrayInputStream(decodeBase64Image(institution.getLogoData()))) {

                IOUtils.copy(is, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException ioe) {
                log.error("Unable to read image for institution {}", id);
                throw new ImageIOException(ioe);
            }
        }

        response.setStatus(HttpStatus.OK.value());
    }

    @ApiOperation(value = "Delete logo image for Institution",
            notes = "Delete logo image for institution",
            response = User.class)
    @DeleteMapping(value = "/{id}/logo")
    public ResponseEntity deleteLogo(@PathVariable("id") final Long id,
                                     HttpServletRequest request) throws Exception {

        isAdminUser(request);

        institutionService.deleteLogo(id);
        return ResponseEntity.noContent().build();
    }

    private static byte[] decodeBase64Image(String image) throws UnsupportedEncodingException {
        return Base64.decodeBase64(image.getBytes("UTF-8"));
    }
}
