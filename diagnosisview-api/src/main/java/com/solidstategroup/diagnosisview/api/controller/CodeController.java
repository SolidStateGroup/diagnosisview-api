package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@RequestMapping("/api")
@Log
public class CodeController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;
    private CodeRepository codeRepository;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param userService UserService manages the dashboard users
     */
    @Autowired
    public CodeController(final UserService userService, final CodeRepository codeRepository) {
        this.userService = userService;
        this.codeRepository = codeRepository;
    }


    /**
     * Create a code within DV.
     *
     * @param code - code to create
     * @return the created code with ID
     * @throws Exception
     */
    @RequestMapping(value = "/code", method = RequestMethod.POST)
    @ApiOperation(value = "Create Code",
            notes = "Creates code within DV (unsure if required)",
            response = User.class)
    public Code createUser(@RequestBody final Code code) throws Exception {
        return null;
    }


    /**
     * @param code
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/code", method = RequestMethod.PUT)
    @ApiOperation(value = "Update Code",
            notes = "Update a user, pass the password in which will then be encrypted",
            response = User.class)
    public Code updateUser(@RequestBody final Code code) throws Exception {
        return null;
    }


    /**
     * Update a code.
     *
     * @param code Code code to delete
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/code", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete code - TEST PURPOSES ONLY",
            notes = "Pass the code in with an ID to be deleted")
    public void deleteUser(@RequestBody final Code code) throws Exception {
    }

    /**
     * Get all codes.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    @ApiOperation(value = "Get All Codes",
            notes = "Admin User endpoint to get all codes within the DiagnosisView",
            response = CodeDto[].class)
    public List<CodeDto> getAllUsers() throws Exception {

        List<CodeDto> codeDtoList = new ArrayList<>();
        List<Code> codeList = codeRepository.findAll();
        codeList.parallelStream().forEach(code -> {
            CodeDto codeDto = new CodeDto();
            codeDto.setCode(code.getCode());
            codeDto.setFriendlyName(code.getPatientFriendlyName());
            codeDto.setDescription(code.getDescription());
        });

        return codeDtoList;
    }


    /**
     * Get a single code by querying using the id.
     *
     * @return User the updated user
     * @throws Exception thrown adding projects config
     */
    @RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
    @ApiOperation(value = "Get A single Codes",
            notes = "Admin User endpoint to get all codes within the DiagnosisView",
            response = Code.class)
    public Code getAllUsers(@PathVariable("code") final String code) throws Exception {
        return codeRepository.findOneByCode(code);
    }

}
