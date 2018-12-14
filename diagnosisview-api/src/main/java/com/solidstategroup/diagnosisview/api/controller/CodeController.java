package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@RequestMapping("/api")
public class CodeController extends BaseController {

    private final CodeService codeService;
    private final LinkService linkService;

    /**
     * Instantiate API controller, includes required services.
     */
    public CodeController(final CodeService codeService,
                          LinkService linkService) {
        super();
        this.codeService = codeService;
        this.linkService = linkService;
    }

    /**
     * Create a code within DV.
     *
     * @param code - code to create
     * @return the created code with ID
     */
    @RequestMapping(value = "/code", method = RequestMethod.POST)
    @ApiOperation(value = "Create Code",
            notes = "Creates code within DV",
            response = Code.class)
    public Code createCode(@RequestBody final Code code) {

        codeService.save(code);

        code.getLinks().forEach(linkService::saveLink);

        return code;
    }

    /**
     * @param code
     * @return
     */
    @RequestMapping(value = "/code", method = RequestMethod.PUT)
    @ApiOperation(value = "Update Code",
            notes = "Update a user, pass the password in which will then be encrypted",
            response = Code.class)
    public Code updateCode(@RequestBody final Code code) {

        codeService.save(code);
        code.getLinks().forEach(linkService::saveLink);

        return code;
    }

    /**
     * Update a code.
     *
     * @param code Code code to delete
     */
    @RequestMapping(value = "/code", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete code - TEST PURPOSES ONLY",
            notes = "Pass the code in with an ID to be deleted")
    public void deleteCode(@RequestBody final Code code) {
        codeService.delete(code);
    }

    /**
     * Get all codes.
     *
     * @return User the updated user
     */
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    @ApiOperation(value = "Get All Codes",
            notes = "Admin User endpoint to get all codes within the DiagnosisView",
            response = CodeDto[].class)
    public List<CodeDto> getAllCodes(HttpServletRequest request) throws Exception {

        User user = checkIsAuthenticated(request);

        if ("University of Edinburgh".equalsIgnoreCase(user.getInstitution())) {
            return codeService.getAll(Institution.UNIVERSITY_OF_EDINBURGH);
        }

        return codeService.getAll(null);
    }

    /**
     * Get all categories.
     *
     * @return List all categories for code
     */
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    @ApiOperation(value = "Get All Categories",
            notes = "Get all categories from DiagnosisView",
            response = CategoryDto[].class)
    public List<CategoryDto> getAllCategories() {
        return codeService.getAllCategories();
    }

    /**
     * Get a single code by querying using the id.
     *
     * @return User the updated user
     */
    @RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
    @ApiOperation(value = "Get A single Codes",
            notes = "Admin User endpoint to get all codes within the DiagnosisView",
            response = Code.class)
    public Code getAllUsers(@PathVariable("code") final String code) {
        return codeService.get(code);
    }

}
