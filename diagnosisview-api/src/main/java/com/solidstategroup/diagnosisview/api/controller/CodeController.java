package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @ApiOperation(value = "Get All activeCodes",
            notes = "User endpoint to get all active codes within the DiagnosisView",
            response = CodeDto[].class)
    @GetMapping("/code")
    public List<CodeDto> getAllActiveCodes(HttpServletRequest request) throws Exception {

        User user = getUserFromRequest(request);

        if (user != null && user.getInstitution() != null) {
            return codeService.getAllActive(user.getInstitution());
        }

        return codeService.getAllActive(null);
    }

    @ApiOperation(value = "Find Codes by synonyms",
            notes = "Admin User endpoint to get all codes within the DiagnosisView by synonyms",
            response = CodeDto[].class)
    @GetMapping("/code/synonyms/{term}")
    public List<CodeDto> findCodesBySynonyms(@PathVariable("term") final String term,
                                             HttpServletRequest request) throws Exception {

        User user = getUserFromRequest(request);

        if (user != null && user.getInstitution() != null) {
            return codeService.getCodesBySynonyms(term, user.getInstitution());
        }

        return codeService.getCodesBySynonyms(term, null);
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
    public Code getCodeByName(@PathVariable("code") final String code, HttpServletRequest request)
            throws Exception {

        User user = getUserFromRequest(request);
        if (user != null && user.getInstitution() != null) {
            return codeService.getByInstitution(code, user.getInstitution());
        }

        return codeService.get(code);
    }
}

