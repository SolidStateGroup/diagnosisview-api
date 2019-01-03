package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/api")
public class CodeController extends BaseController {

    private final CodeService codeService;
    private final LinkService linkService;

    public CodeController(final UserService userService,
                          final CodeService codeService,
                          final LinkService linkService) {

        super(userService);
        this.codeService = codeService;
        this.linkService = linkService;
    }

    @ApiOperation(value = "Create Code",
            notes = "Creates/Updates code within DV",
            response = Code.class)
    @RequestMapping(value = "/code", method = {PUT, POST})
    public Code upsertCode(@RequestBody final Code code) {

        codeService
                .save(code)

                .getLinks()
                .forEach(linkService::update);

        return code;
    }

    @ApiOperation(value = "Delete code - TEST PURPOSES ONLY",
            notes = "Pass the code in with an ID to be deleted")
    @DeleteMapping("/code")
    public void deleteCode(@RequestBody final Code code) {
        codeService.delete(code);
    }

    @ApiOperation(value = "Get All Codes",
            notes = "Admin User endpoint to get all codes within the DiagnosisView",
            response = CodeDto[].class)
    @GetMapping("/code")
    public List<CodeDto> getAllCodes(HttpServletRequest request) throws Exception {

        User user = checkIsAuthenticated(request);

        if ("University of Edinburgh".equalsIgnoreCase(user.getInstitution())) {
            return codeService.getAll(Institution.UNIVERSITY_OF_EDINBURGH);
        }

        return codeService.getAll(null);
    }

    @ApiOperation(value = "Get All Categories",
            notes = "Get all categories from DiagnosisView",
            response = CategoryDto[].class)
    @GetMapping("/category")
    public List<CategoryDto> getAllCategories() {
        return codeService.getAllCategories();
    }

    @ApiOperation(value = "Get A single Codes",
            notes = "Admin endpoint to get a code by it's name",
            response = CodeDto.class)
    @GetMapping("/code/{code}")
    public Code getCodeByName(@PathVariable("code") final String code) {

        return codeService.get(code);
    }
}


