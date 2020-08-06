package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.service.UserService;
import com.solidstategroup.diagnosisview.service.impl.InstitutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
}
